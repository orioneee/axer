package io.github.orioneee.presentation.selectdevice

import androidx.lifecycle.viewModelScope
import com.malinskiy.adam.AndroidDebugBridgeClientFactory
import com.malinskiy.adam.request.device.AsyncDeviceMonitorRequest
import com.malinskiy.adam.request.device.Device
import com.malinskiy.adam.request.device.DeviceState
import com.malinskiy.adam.request.forwarding.ListPortForwardsRequest
import com.malinskiy.adam.request.forwarding.LocalTcpPortSpec
import com.malinskiy.adam.request.forwarding.PortForwardRequest
import com.malinskiy.adam.request.forwarding.PortForwardingMode
import com.malinskiy.adam.request.forwarding.PortForwardingRule
import com.malinskiy.adam.request.forwarding.RemoteTcpPortSpec
import com.malinskiy.adam.request.forwarding.RemovePortForwardRequest
import com.malinskiy.adam.request.prop.GetSinglePropRequest
import io.github.orioneee.domain.other.DeviceData
import io.github.orioneee.models.AdbDevice
import io.github.orioneee.models.CreatedPortForwardingRules
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.ServerSocket

private val adb = AndroidDebugBridgeClientFactory().build()
internal actual fun DeviceScanViewModel.lookForAdbConnectAxer() {
    val deviceEventsChannel: ReceiveChannel<List<Device>> = adb.execute(
        request = AsyncDeviceMonitorRequest(),
        scope = viewModelScope
    )

    viewModelScope.launch {
        for (currentDeviceList in deviceEventsChannel) {
            val filtered = currentDeviceList.filter {
                it.state == DeviceState.DEVICE
            }
            if (filtered.isNotEmpty()) {
                filtered.map {
                    async {
                        try {
                            val deviceName: String = adb.execute(
                                request = GetSinglePropRequest(name = "ro.product.model"),
                                serial = it.serial
                            )
                            println("Device name for ${it.serial}: $deviceName")
                            AdbDevice(
                                serial = it.serial,
                                name = deviceName.ifEmpty { "Unknown Device" }.replace("\n", "")
                            )
                        } catch (e: Exception) {
                            println("Error getting device info for ${it.serial}: ${e.message}")
                            null
                        }
                    }
                }
                    .awaitAll()
                    .filterNotNull()
                    .let {
                        _adbDevices.value = it
                    }
            }
        }
    }
}

internal actual suspend fun DeviceScanViewModel.checkIsAxerOnAdb(
    device: AdbDevice,
    port: Int,
    localClient: HttpClient
): Result<io.github.orioneee.models.Device> {
    try {
        val createdRules = _createPortForwaringRules.value
        createdRules.forEach {
            println("Removing existing port forwarding rule: $it")
            adb.execute(
                request = RemovePortForwardRequest(
                    local = LocalTcpPortSpec(it.localPort),
                    serial = it.serial
                )
            )
        }
        val existingRules: List<PortForwardingRule> = adb.execute(ListPortForwardsRequest(device.serial))

        val desiredRemoteSpec = RemoteTcpPortSpec(port)
        val existingRule = existingRules.firstOrNull { it.remoteSpec == desiredRemoteSpec }

        val forwardedPort = if (existingRule != null) {
            (existingRule.localSpec as? LocalTcpPortSpec)?.port ?: findFreePort()
        } else {
            val availablePort = findFreePort()
            adb.execute(
                request = PortForwardRequest(
                    local = LocalTcpPortSpec(availablePort), // host port
                    remote = RemoteTcpPortSpec(port),        // device port
                    mode = PortForwardingMode.DEFAULT,
                    serial = device.serial
                )
            )
            availablePort
        }

        val result = checkIfAxerAvailable(forwardedPort, localClient)

        result.onFailure {
            println("Axer check failed on port $forwardedPort: ${it.message}")
            adb.execute(
                request = RemovePortForwardRequest(
                    local = LocalTcpPortSpec(forwardedPort),
                    serial = device.serial
                )
            )
        }

        return result.fold(
            onSuccess = {
                println("Axer is available on port $forwardedPort: $it")
                _createPortForwaringRules.update { rules ->
                    rules + CreatedPortForwardingRules(
                        serial = device.serial,
                        localPort = forwardedPort,
                        remotePort = port
                    )
                }
                Result.success(
                    io.github.orioneee.models.Device(
                        data = it,
                        connection = io.github.orioneee.models.ConnectionInfo(
                            ip = "127.0.0.1",
                            port = forwardedPort,
                        )
                    )
                )
            },
            onFailure = {
                Result.failure(it)
            }
        )
    } catch (e: Exception) {
        println("Unexpected error during Axer check: ${e.message}")
        return Result.failure(e)
    }
}

private fun findFreePort(): Int {
    ServerSocket(0).use { socket ->
        return socket.localPort
    }
}

private suspend fun checkIfAxerAvailable(port: Int, client: HttpClient): Result<DeviceData> {
    try {
        val response: DeviceData = client.get("http://localhost:$port/isAxerServer").body()
        return Result.success(response)
    } catch (e: Exception) {
        e.printStackTrace()
        return Result.failure(e)
    }
}