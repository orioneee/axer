package io.github.orioneee.presentation.selectdevice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.orioneee.data.RemoteRepositoryImpl
import io.github.orioneee.domain.other.DeviceData
import io.github.orioneee.models.ConnectionInfo
import io.github.orioneee.models.Device
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.concurrent.atomic.AtomicInteger

class DeviceScanViewModel : ViewModel() {
    private val allConnections = generateConnections()
    private val _isShowingNewVersionDialog = MutableStateFlow(false)
    private val _manuallyAddedConnections = MutableStateFlow<List<ConnectionInfo>>(emptyList())
    private val _isShowAddDeviceDialog = MutableStateFlow(false)
    private val _foundedDevices = MutableStateFlow<List<Device>>(emptyList())
    private val _scanningProgress = MutableStateFlow(0)
    private val _manuallyAddedConnectionsScanProgress = MutableStateFlow(0)
    private val _manuallyAddedFoundedDevices = MutableStateFlow<List<Device>>(emptyList())
    private val _isAutoScanning = MutableStateFlow(false)
    private val _isScanningManuallyAddedConnections = MutableStateFlow(false)

    val isShowingNewVersionDialog = _isShowingNewVersionDialog.asStateFlow()
    val isShowAddDeviceDialog = _isShowAddDeviceDialog.asStateFlow()
    val isScanning = combine(
        _isAutoScanning,
        _isScanningManuallyAddedConnections
    ) { isAutoScanning, isScanningManuallyAdded ->
        isAutoScanning || isScanningManuallyAdded
    }
    val scanningProgress = combine(
        _scanningProgress,
        _manuallyAddedConnectionsScanProgress
    ) { scanning, manuallyAdded ->
        (scanning.toFloat() + manuallyAdded.toFloat()) / (allConnections.size + _manuallyAddedConnections.value.size)
    }
    val foundedDevices = combine(
        _manuallyAddedFoundedDevices,
        _foundedDevices
    ) { manuallyAdded, founded ->
        (manuallyAdded + founded).distinctBy { it.connection.ip }
    }
    val manuallyAddedButNotAxer = combine(
        _manuallyAddedConnections,
        _manuallyAddedFoundedDevices
    ) { manuallyAdded, founded ->
        manuallyAdded.filter { conn ->
            founded.none { it.connection.ip == conn.ip }
        }
    }

    fun onDismissNewVersion() {
        _isShowingNewVersionDialog.value = false
    }

    fun onClickAddDevice() {
        _isShowAddDeviceDialog.value = true
    }

    fun onAddedConnection(device: ConnectionInfo) {
        _manuallyAddedConnections.update { it + device }
        _isShowAddDeviceDialog.value = false
    }

    fun onDismissAddDevice() {
        _isShowAddDeviceDialog.value = false
    }

    fun onDeleteConnection(device: ConnectionInfo) {
        _manuallyAddedConnections.update { it - device }
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
        _manuallyAddedConnections
            .onEach {
                scanForAxerManuallyAddedConnections()
            }
            .launchIn(viewModelScope)

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
                        _isShowingNewVersionDialog.value = true
                    }
                } else {
                    println("Failed to fetch latest version: ${latestTag.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    var autoScanningJob: Job? = null
    var manuallyAddedScanningJob: Job? = null
    fun scanLocalNetwork() {
        autoScanningJob?.cancel()
        autoScanningJob = viewModelScope.launch(Dispatchers.IO) {
            scanForAxerDevices()
        }
    }

    fun scanManuallyAddedConnections() {
        manuallyAddedScanningJob?.cancel()
        manuallyAddedScanningJob = viewModelScope.launch(Dispatchers.IO) {
            scanForAxerManuallyAddedConnections()
        }
    }

    fun getSubNetMask(): List<String> = NetworkInterface.getNetworkInterfaces().asSequence()
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
            it.substringAfterLast(".")
                .toIntOrNull()
        }

    private fun generateConnections(): List<ConnectionInfo> {
        val subnets = getSubNetMask()


        val totalJobsPerSubnet = 254


        return subnets.flatMap { subnet ->
            (1..totalJobsPerSubnet).map { i ->
                ConnectionInfo("$subnet.$i")
            }
        }
    }

    private suspend fun scanForAxerManuallyAddedConnections() = withContext(Dispatchers.IO) {
        _isScanningManuallyAddedConnections.value = true
        _manuallyAddedFoundedDevices.value = emptyList()
        val completedJobs = AtomicInteger(0)
        val founded = mutableListOf<Device>()
        _manuallyAddedConnections.value.map { conn ->
            async {
                ensureActive()
                checkConnection(conn).let { result ->
                    _manuallyAddedConnectionsScanProgress.value =
                        completedJobs.incrementAndGet()
                    if(result != null){
                        founded += Device(
                            connection = conn,
                            data = result,
                        )
                        _manuallyAddedFoundedDevices.update {
                            founded
                        }
                    }
                }
            }
        }.awaitAll()
        _isScanningManuallyAddedConnections.value = false
    }

    private suspend fun scanForAxerDevices() = withContext(Dispatchers.IO) {
        val currentDevices = _foundedDevices.value
        _isAutoScanning.value = true
        _foundedDevices.value = emptyList()
        val devices = allConnections.sortedBy { conn ->
            if (currentDevices.any { it.connection.toAddress() == conn.toAddress() }) {
                currentDevices.indexOfFirst { it.connection.toAddress() == conn.toAddress() }
            } else {
                Int.MAX_VALUE
            }
        }
        val founded = mutableListOf<Device>()
        val completedJobs = AtomicInteger(0)
        devices.map { conn ->
            async {
                checkConnection(conn).let { result ->
                    _scanningProgress.value = completedJobs.incrementAndGet()
                    if(result != null){
                        founded += Device(
                            connection = conn,
                            data = result,
                        )
                        _foundedDevices.update {
                            founded
                        }
                    }
                }
            }
        }.awaitAll()
        _isAutoScanning.value = false
    }

    private suspend fun checkConnection(
        data: ConnectionInfo,
    ): DeviceData? {
        fun doInNeededIp(action: () -> Unit) {
            if (data.ip == "192.168.0.238" || data.ip == "192.168.0.165") {
                action()
            }
        }
        return try {
            val response = localClient.get("${data.toAddress()}/isAxerServer")
            if (response.status.value == HttpURLConnection.HTTP_OK) {
                response.body<DeviceData>()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}