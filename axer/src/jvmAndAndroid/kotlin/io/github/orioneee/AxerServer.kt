package io.github.orioneee

import androidx.compose.material3.SnackbarDuration
import io.github.orioneee.axer.generated.resources.Res
import io.github.orioneee.axer.generated.resources.axer_already_running
import io.github.orioneee.axer.generated.resources.axer_port_in_use
import io.github.orioneee.axer.generated.resources.server_failed
import io.github.orioneee.axer.generated.resources.server_started
import io.github.orioneee.axer.generated.resources.server_stopped
import io.github.orioneee.internal.domain.other.AxerServerStatus
import io.github.orioneee.internal.domain.other.DeviceData
import io.github.orioneee.internal.domain.other.EnabledFeathers
import io.github.orioneee.internal.koin.IsolatedContext
import io.github.orioneee.internal.processors.RoomReader
import io.github.orioneee.internal.remote.server.databaseModule
import io.github.orioneee.internal.remote.server.exceptionsModule
import io.github.orioneee.internal.remote.server.getDeviceData
import io.github.orioneee.internal.remote.server.logsModule
import io.github.orioneee.internal.remote.server.requestsModule
import io.github.orioneee.internal.room.dao.AxerExceptionDao
import io.github.orioneee.internal.room.dao.LogsDAO
import io.github.orioneee.internal.room.dao.RequestDao
import io.github.orioneee.internal.storage.AxerSettings
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.cio.CIOApplicationEngine
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.plugins.origin
import io.ktor.server.request.path
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.sendSerialized
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.getString
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.ServerSocket
import kotlin.time.Duration.Companion.seconds

internal fun getLocalIpAddress(): String? {
    return NetworkInterface.getNetworkInterfaces().toList().flatMap { it.inetAddresses.toList() }
        .firstOrNull {
            !it.isLoopbackAddress && it is InetAddress && it.hostAddress.indexOf(':') < 0
        }?.hostAddress
}

internal var serverJob: Job? = null

internal expect fun serverNotify(
    message: String,
    duration: SnackbarDuration = SnackbarDuration.Long
)

internal fun isPortInUse(port: Int): Boolean {
    return try {
        val socket = ServerSocket(port)
        socket.close()
        false // port is free
    } catch (e: Exception) {
        true // port is in use
    }
}


internal suspend fun checkIfAnotherAxerInstanceIsRunning(port: Int): Pair<Boolean, String?> {
    return try {
        val client = HttpClient()
        val response = client.get("http://127.0.0.1:$port/isAxerServer")
        val body = response.body<String>()
        val data = Json.decodeFromString<DeviceData>(body)
        client.close()
        println("Axer server is running: ${data.baseAppName} on port $port")
        true to data.baseAppName
    } catch (e: Exception) {
        false to null
    }
}

internal expect suspend fun sendNotificationAboutRunningServer(
    ip: String,
    port: Int,
    isRunning: Boolean
)

private val _isServerRunning = MutableStateFlow<AxerServerStatus>(AxerServerStatus.Stopped)
internal val isAxerServerRunning = _isServerRunning.asStateFlow()

private fun getEntity(
    requests: Boolean,
    exceptions: Boolean,
    logs: Boolean,
    database: Boolean,
    isReadOnly: Boolean
): EnabledFeathers {
    return EnabledFeathers(
        isEnabledRequests = requests,
        isEnabledExceptions = exceptions,
        isEnabledLogs = logs,
        isEnabledDatabase = database,
        isReadOnly = isReadOnly
    )
}


fun Axer.runServerIfNotRunning(
    scope: CoroutineScope,
    port: Int = AXER_SERVER_PORT,
    readOnly: Boolean = false,
    sendInfoMessages: Boolean = true
) {
    if (serverJob == null || serverJob?.isCompleted == true) {
        serverJob = scope.launch(SupervisorJob() + Dispatchers.IO) {
            val canRun = runChecksBeforeStartingServer(port)
            if (!canRun) return@launch

            val ip = getLocalIpAddress() ?: "localhost"
            var server: EmbeddedServer<CIOApplicationEngine, CIOApplicationEngine.Configuration>? =
                null
            try {
                _isServerRunning.value = AxerServerStatus.Started(port)
                coroutineScope {
                    val startedMsg = getString(Res.string.server_started, "$ip:$port")
                    server = getKtorServer(port, readOnly, sendInfoMessages)
                    if (sendInfoMessages) {
                        serverNotify(startedMsg)
                    }
                    server.start(wait = true)
                }
            } catch (e: CancellationException) {
                val stoppedMsg = getString(Res.string.server_stopped)
                if (sendInfoMessages) {
                    serverNotify(stoppedMsg)
                }
                server?.stop(1000, 1000)
                throw e
            } catch (e: Exception) {
                val failedMsg = getString(Res.string.server_failed, e.message ?: "unknown error")
                if (sendInfoMessages) {
                    serverNotify(failedMsg)
                }
                server?.stop()
                e.printStackTrace()
            } finally {
                _isServerRunning.value = AxerServerStatus.Stopped
                connectedClients.value = emptyMap()
                server?.stop(1000, 1000)
            }
        }.also {
            it.invokeOnCompletion { cause ->
                _isServerRunning.value = AxerServerStatus.Stopped
            }
        }
    }
}

fun Axer.stopServerIfRunning(
    sendInfoMessages: Boolean = true
) {
    if (serverJob != null && serverJob?.isActive == true) {
        serverJob?.cancel()
        serverJob = null
        if (sendInfoMessages) {
            serverNotify("Axer server stopped")
        }
    } else {
        if (sendInfoMessages) {
            serverNotify("Axer server is not running")
        }
    }
    _isServerRunning.value = AxerServerStatus.Stopped
    connectedClients.value = emptyMap()
}

private val connectedClients: MutableStateFlow<Map<String, List<String>>> = MutableStateFlow(emptyMap())



internal suspend fun runChecksBeforeStartingServer(port: Int): Boolean {
    val (isRunning, appName) = checkIfAnotherAxerInstanceIsRunning(port)
    if (isRunning) {
        val msg = if (appName != null) {
            getString(Res.string.axer_already_running, port, appName)
        } else {
            getString(Res.string.axer_already_running, port, "Unknown app")
        }
        Axer.e("AxerServer", "Axer already running on port $port: $appName")
        Axer.recordException(IllegalStateException(msg))
        serverNotify(msg)
        return false
    }

    if (isPortInUse(port)) {
        val msg = getString(Res.string.axer_port_in_use, port)
        serverNotify(msg)
        Axer.e("AxerServer", "Port $port is already in use")
        Axer.recordException(IllegalStateException(msg))
        return false
    }

    return true
}


/**
 * @suppress
 */
const val AXER_SERVER_PORT = 53214
private val addClientMutex = Mutex()

@OptIn(FlowPreview::class)
internal fun CoroutineScope.getKtorServer(
    port: Int,
    readOnly: Boolean,
    sendInfoMessages: Boolean
): EmbeddedServer<CIOApplicationEngine, CIOApplicationEngine.Configuration> {
    val reader = RoomReader()
    val requestDao: RequestDao by IsolatedContext.koin.inject()
    val exceptionsDao: AxerExceptionDao by IsolatedContext.koin.inject()
    val logDao: LogsDAO by IsolatedContext.koin.inject()

    val isEnabledRequests = AxerSettings.enableRequestMonitor.asFlow()
    val isEnabledExceptions = AxerSettings.enableExceptionMonitor.asFlow()
    val isEnabledLogs = AxerSettings.enableLogMonitor.asFlow()
    val isEnabledDatabase = AxerSettings.enableDatabaseMonitor.asFlow()
    val myJson = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = true
    }


    fun onAddClient(call: ApplicationCall) {
        val ip = call.request.origin.remoteHost
        val path = call.request.path()
        launch {
            addClientMutex.withLock {
                val inList = connectedClients.value[ip] ?: emptyList()
                if (inList.isEmpty()) {
                    connectedClients.update {
                        val current = it.toMutableMap()
                        current[ip] = listOf(path)
                        current
                    }
                } else if (!inList.contains(path)) {
                    connectedClients.update {
                        val current = it.toMutableMap()
                        current[ip] = inList + path
                        current
                    }
                }
                println("Added client with IP: $ip and path: $path")
            }
        }
    }

    fun onRemoveClient(
        call: ApplicationCall
    ) {
        val ip = call.request.origin.remoteHost
        val path = call.request.path()
        launch {
            addClientMutex.withLock {
                connectedClients.update { currentClients ->
                    val updatedClients = currentClients.toMutableMap()
                    val inList = updatedClients[ip] ?: emptyList()
                    if (inList.isNotEmpty()) {
                        updatedClients[ip] = inList - path
                        if (updatedClients[ip]?.isEmpty() == true) {
                            updatedClients.remove(ip)
                        }
                    }
                    updatedClients
                }
                println("Removed client with IP: $ip and path: $path")
            }
        }
    }

    return embeddedServer(CIO, port = port) {
        install(WebSockets) {
            pingPeriod = 15.seconds
            timeout = 15.seconds
            maxFrameSize = Long.MAX_VALUE
            masking = false
            contentConverter = KotlinxWebsocketSerializationConverter(myJson)
        }
        install(ContentNegotiation) {
            json(myJson)
        }
        install(DefaultHeaders) {
            header("Content-Type", "application/json")
        }
        var previousClients: Set<String> = emptySet()

        launch {
            connectedClients
                .map { it.keys }
                .sample(1.seconds)
                .distinctUntilChanged()
                .collect { currentClients ->
                    val added = currentClients - previousClients
                    val removed = previousClients - currentClients

                    previousClients = currentClients

                    val message = buildString {
                        if (added.isNotEmpty()) {
                            append("Connected: ${added.joinToString("\n")}")
                        }
                        if (removed.isNotEmpty()) {
                            append("Disconnected: ${removed.joinToString("\n")}")
                        }
                    }.let {
                        if(it.endsWith("\n")) it.dropLast(1) else it
                    }
                    if (message.isNotEmpty() && sendInfoMessages) {
                        serverNotify(message, SnackbarDuration.Short)
                    }
                }
        }

        routing {
            requestsModule(
                isEnabledRequests = isEnabledRequests,
                requestsDao = requestDao,
                readOnly = readOnly,
                onAddClient = ::onAddClient,
                onRemoveClient = ::onRemoveClient,
            )
            exceptionsModule(
                isEnabledExceptions = isEnabledExceptions,
                exceptionsDao = exceptionsDao,
                readOnly = readOnly,
                onAddClient = ::onAddClient,
                onRemoveClient = ::onRemoveClient,
            )
            logsModule(
                isEnabledLogs = isEnabledLogs,
                logsDao = logDao,
                readOnly = readOnly,
                onAddClient = ::onAddClient,
                onRemoveClient = ::onRemoveClient,
            )
            databaseModule(
                isEnabledDatabase = isEnabledDatabase,
                reader = reader,
                readOnly = readOnly,
                onAddClient = ::onAddClient,
                onRemoveClient = ::onRemoveClient,
            )

            get("/isAxerServer") {
                try {
                    val deviceData = getDeviceData(readOnly)
                    println("data: $deviceData")
                    call.respond(HttpStatusCode.OK, getDeviceData(readOnly))
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.InternalServerError, "Error: ${e.message}")
                }
            }

            webSocket("/ws/isAlive") {
                onAddClient(call)
                launch {
                    try {
                        while (true) {
                            ensureActive()
                            delay(1000L)
                            val time = System.currentTimeMillis()
                            sendSerialized("ping - $time")
                        }
                    } catch (e: Exception) {
                    }
                }

                try {
                    for (frame in incoming) {
                    }
                } finally {
                    onRemoveClient(call)
                }
            }
            webSocket("/ws/feathers") {
                sendSerialized(
                    getEntity(
                        requests = isEnabledRequests.first(),
                        exceptions = isEnabledExceptions.first(),
                        logs = isEnabledLogs.first(),
                        database = isEnabledDatabase.first(),
                        isReadOnly = readOnly
                    )
                )
                onAddClient(call)

                launch {
                    combine(
                        isEnabledRequests,
                        isEnabledExceptions,
                        isEnabledLogs,
                        isEnabledDatabase
                    ) { requests, exceptions, logs, database ->
                        getEntity(requests, exceptions, logs, database, readOnly)
                    }.collect { feathers ->
                        sendSerialized(feathers)
                    }
                }

                try {
                    for (frame in incoming) {
                    }
                } finally {
                    onRemoveClient(call)
                }
            }
        }
    }
}