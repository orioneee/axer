package io.github.orioneee.models

import io.github.orioneee.domain.other.DeviceData
import io.github.orioneee.remote.server.AXER_SERVER_PORT
import kotlinx.serialization.Serializable

data class AdbDevice(
    val serial: String,
    val name: String,
)

@Serializable
data class ConnectionInfo(
    val ip: String,
    val port: Int = AXER_SERVER_PORT,
    val isCustomAdded: Boolean = port != AXER_SERVER_PORT,
){
    fun toAddress(): String {
        return "http://$ip:$port"
    }
}

@Serializable
data class Device(
    val connection: ConnectionInfo,
    val data: DeviceData
){
    fun toNavArguments(): NavArgumentsDTO {
        return NavArgumentsDTO(
            device_isAxer = data.isAxer,
            device_osName = data.osName,
            device_osVersion = data.osVersion,
            device_deviceModel = data.deviceModel,
            device_deviceManufacturer = data.deviceManufacturer,
            device_deviceName = data.deviceName,
            device_axerVersion = data.axerVersion,

            info_ip = connection.ip,
            info_port = connection.port,
            info_baseAppName = data.baseAppName
        )
    }
}

@Serializable
data class NavArgumentsDTO(
    val device_isAxer: Boolean,
    val device_osName: String,
    val device_osVersion: String,
    val device_deviceModel: String,
    val device_deviceManufacturer: String,
    val device_deviceName: String,
    val device_axerVersion: String,

    val info_ip: String,
    val info_port: Int,
    val info_baseAppName: String?,
){
    fun toDevice(): Device {
        return Device(
            connection = ConnectionInfo(
                ip = info_ip,
                port = info_port
            ),
            data = DeviceData(
                isAxer = device_isAxer,
                osName = device_osName,
                osVersion = device_osVersion,
                deviceModel = device_deviceModel,
                deviceManufacturer = device_deviceManufacturer,
                deviceName = device_deviceName,
                axerVersion = device_axerVersion,
                baseAppName = info_baseAppName
            )
        )
    }
}