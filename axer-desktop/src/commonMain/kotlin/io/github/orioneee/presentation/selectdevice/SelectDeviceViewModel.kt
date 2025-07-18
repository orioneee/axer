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
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.InetAddress
import java.util.concurrent.atomic.AtomicInteger

// DeviceScanViewModel.kt

// Модель для представления состояния экрана
data class DeviceScanUiState(
    val devices: List<DeviceData> = emptyList(),
    val isSearching: Boolean = false,
    val progress: Float = 0f, // Прогресс сканирования от 0.0 до 1.0
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
                    json(
                        Json {
                            ignoreUnknownKeys = true
                        }
                    )
                }
            }

            val reachableIps = mutableListOf<DeviceData>()

            val subnet = try {
                val localHost = InetAddress.getLocalHost()
                onProgress(emptyList(), 0.1f)
                val ipAddr = localHost.address
                val subnetString = String.format(
                    "%d.%d.%d",
                    (ipAddr[0].toInt() and 0xFF),
                    (ipAddr[1].toInt() and 0xFF),
                    (ipAddr[2].toInt() and 0xFF)
                )
                onProgress(emptyList(), 0.3f)
                subnetString
            } catch (e: Exception) {
                e.printStackTrace()
                onProgress(emptyList(), 1.0f)
                return@withContext
            }

            val totalJobs = 254
            val completedJobs = AtomicInteger(0)

            val jobs = (1..totalJobs).map { i ->
                async {
                    val ip = "$subnet.$i"
                    var partialProgress = 0f

                    try {
                        val inet = InetAddress.getByName(ip)
                        if (inet.isReachable(100)) {
                            partialProgress += 0.5f
                            val done = completedJobs.incrementAndGet()
                            val scanProgress = (done - 1 + partialProgress) / totalJobs
                            val totalProgress = 0.3f + scanProgress * 0.7f
                            onProgress(
                                synchronized(reachableIps) { reachableIps.toList() },
                                totalProgress
                            )

                            try {
                                val url = "http://$ip:9000/isAxerServer"
                                val resp = client.get(url)
                                if (resp.status.value == 200) {
                                    val deviceData: DeviceData = resp.body()
                                    println("Found Axer device at $ip: $deviceData")
                                    synchronized(reachableIps) {
                                        reachableIps.add(deviceData.copy(ip = ip))
                                    }
                                }
                            } catch (_: Exception) { /* Ignore */
                            }
                            partialProgress += 0.5f
                        }
                    } catch (_: Exception) { /* Ignore */
                    }

                    val done = completedJobs.incrementAndGet()
                    val scanProgress = (done - 1 + partialProgress).toFloat() / totalJobs
                    val totalProgress = 0.3f + scanProgress * 0.7f
                    onProgress(
                        synchronized(reachableIps) { reachableIps.toList() },
                        totalProgress
                    )
                }
            }


            jobs.awaitAll()
        }


    init {
        startScanning()
    }
}