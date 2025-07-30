package io.github.orioneee.presentation.selectdevice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.orioneee.data.RemoteRepositoryImpl
import io.github.orioneee.domain.other.DeviceData
import io.github.orioneee.remote.server.AXER_SERVER_PORT
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import io.orioneee.axer.debugger.BuildKonfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger


data class DeviceScanUiState(
    val devices: List<DeviceData> = emptyList(),
    val isSearching: Boolean = false,
    val progress: Float = 0f,
)

class DeviceScanViewModel : ViewModel() {
    private val _newerVersionAvailable = MutableStateFlow<String?>(null)
    private val _isShowingNewVersionDialog = MutableStateFlow(false)

    val newerVersionAvailable = _newerVersionAvailable.asStateFlow()
    val isShowingNewVersionDialog = _isShowingNewVersionDialog.asStateFlow()
    fun onDismissNewVersion() {
        _isShowingNewVersionDialog.value = false
    }

    private val localClient = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(HttpTimeout) {
            connectTimeoutMillis = 1000
            requestTimeoutMillis = 3_500
        }
    }

    private val remoteClient = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    private val remoteRepository: RemoteRepositoryImpl by lazy {
        RemoteRepositoryImpl(remoteClient)
    }

    init {
        viewModelScope.launch {
            try {
                val latestTag = remoteRepository.getLatestGitTag()
                val currentVersion = BuildKonfig.VERSION_NAME
                val currentVersionCode =
                    currentVersion.split(".").joinToString("").toIntOrNull() ?: 0
                if (latestTag.isSuccess) {
                    val latestVersion = latestTag.getOrThrow()
                    val latestVersionCode = latestVersion.split(".")
                        .joinToString("")
                        .toIntOrNull() ?: 0
                    println("Current version: $currentVersion, Latest version: $latestVersion")
                    println("Current version code: $currentVersionCode, Latest version code: $latestVersionCode")
                    if (latestVersionCode > currentVersionCode) {
                        _newerVersionAvailable.value = latestVersion
                        _isShowingNewVersionDialog.value = true
                    }
                } else {
                    println("Failed to fetch latest version: ${latestTag.exceptionOrNull()?.message}")
                    _newerVersionAvailable.value = null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _newerVersionAvailable.value = null
            }
        }
    }

    private val _uiState = MutableStateFlow(DeviceScanUiState())
    val uiState = _uiState.asStateFlow()

    var scanningJob: Job? = null

    fun startScanning() {
        scanningJob = viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true, devices = emptyList(), progress = 0f) }

            scanForAxerDevices { foundDevices, currentProgress ->
                _uiState.update {
                    ensureActive()
                    it.copy(devices = foundDevices, progress = currentProgress)
                }
            }

            _uiState.update { it.copy(isSearching = false) }
        }
    }

    private suspend fun scanForAxerDevices(onProgress: (List<DeviceData>, Float) -> Unit) =
        withContext(Dispatchers.IO) {
            val reachableIps = CopyOnWriteArrayList<DeviceData>()

            val subnets = NetworkInterface.getNetworkInterfaces().asSequence()
                .filter {
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


            val totalJobsPerSubnet = 254
            val totalJobs = subnets.size * totalJobsPerSubnet
            val completedJobs = AtomicInteger(0)

            val allIps = subnets.flatMap { subnet ->
                (1..totalJobsPerSubnet).map { i ->
                    "$subnet.$i"
                }
            }.sortedBy { ip ->
                if (uiState.value.devices.map { it.ip }.contains(ip)) {
                    0
                } else {
                    1
                }
            }

            allIps.map {
                async {
                    try {
                        checkIp(it, localClient)?.let { result ->
                            println("Found Axer server at: $it")
                            reachableIps.add(result)
                        }
                    } finally {
                        val currentProgress = completedJobs.incrementAndGet().toFloat() / totalJobs
                        onProgress(reachableIps.toList(), currentProgress)
                    }
                }
            }.awaitAll()
        }

    private suspend fun checkIp(ip: String, client: HttpClient): DeviceData? {
        fun doInNeededIp(action: () -> Unit) {
            if (ip == "192.168.0.238" || ip == "192.168.0.165") {
                action()
            }
        }
        return try {
            val response = client.get("http://$ip:$AXER_SERVER_PORT/isAxerServer")
            if (response.status.value == HttpURLConnection.HTTP_OK) {
                doInNeededIp {
                    println("Axer server found at: $ip")
                }
                response.body<DeviceData>().copy(ip = ip, port = AXER_SERVER_PORT)
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
            null
        }
    }
}