package io.github.orioneee.presentation.selectdevice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.orioneee.domain.other.DeviceData
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.CopyOnWriteArrayList


// Data class remains the same
data class DeviceData(
    val deviceName: String? = null,
    val osName: String? = null,
    val osVersion: String? = null,
    val ip: String? = null
)

data class DeviceScanUiState(
    val devices: List<DeviceData> = emptyList(),
    val isSearching: Boolean = false,
    val progress: Float = 0f, // Scan progress from 0.0 to 1.0
)

class DeviceScanViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DeviceScanUiState())
    val uiState = _uiState.asStateFlow()

    fun startScanning() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true, devices = emptyList(), progress = 0f) }

            scanForAxerDevices { foundDevices, currentProgress ->
                _uiState.update {
                    it.copy(devices = foundDevices, progress = currentProgress)
                }
            }

            _uiState.update { it.copy(isSearching = false) }
        }
    }

    private suspend fun scanForAxerDevices(onProgress: (List<DeviceData>, Float) -> Unit) =
        withContext(Dispatchers.IO) {
            val client = HttpClient {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
            }

            val reachableIps = CopyOnWriteArrayList<DeviceData>()

            val subnets = NetworkInterface.getNetworkInterfaces().asSequence()
                .filter {
                    //check not contains VMware Network
                    !it.displayName.contains("VMware", ignoreCase = true)
                            && !it.displayName.contains("VirtualBox", ignoreCase = true)
                }
                .filter { it.isUp && !it.isLoopback }
                .flatMap { nif ->
                    nif.inetAddresses.asSequence()
                        .filterIsInstance<Inet4Address>()
                        .filter { it.isSiteLocalAddress }
                        .map { it.hostAddress.substringBeforeLast('.') }
                }
                .distinct()
                .toList()
                .sortedBy {
                    it.substringAfterLast(".").toIntOrNull()
                }


            if (subnets.isEmpty()) {
                client.close()
                return@withContext
            }

            val totalJobsPerSubnet = 254
            val totalJobs = subnets.size * totalJobsPerSubnet
            val completedJobs = AtomicInteger(0)


            subnets.map { subnet ->
                val ipJobs = (1..totalJobsPerSubnet).map { i ->
                    async {
                        val ip = "$subnet.$i"
                        try {
                            checkIp(ip, client)?.let { result ->
                                println("Found Axer server at: $ip")
                                reachableIps.add(result)
                            }
                        } finally {
                            val currentProgress =
                                completedJobs.incrementAndGet().toFloat() / totalJobs
                            onProgress(reachableIps.toList(), currentProgress)
                        }
                    }
                }
                ipJobs.awaitAll()
            }

            client.close()
        }

    private suspend fun checkIp(ip: String, client: HttpClient): DeviceData? {
        fun doInNeededIp(action: () -> Unit) {
            if (ip == "192.168.0.182" || ip == "192.168.0.165") {
                action()
            }
        }
        return try {
            if (!InetAddress.getByName(ip).isReachable(200)) {
                doInNeededIp {
                    println("Skipping unreachable IP: $ip")
                }
                return null
            }
            val response = client.get("http://$ip:9000/isAxerServer")
            if (response.status.value == HttpURLConnection.HTTP_OK) {
                doInNeededIp {
                    println("Axer server found at: $ip")
                }
                response.body<DeviceData>().copy(ip = ip)
            } else {
                doInNeededIp {
                    println("No Axer server at: $ip, status: ${response.status.value}")
                }
                null
            }
        } catch (e: Exception) {
            doInNeededIp {
                println("Error checking IP $ip: ${e.message}")
            }
            null // Catches timeouts, connection refused, etc.
        }
    }

    // Initial scan when ViewModel is created.
    init {
        startScanning()
    }
}