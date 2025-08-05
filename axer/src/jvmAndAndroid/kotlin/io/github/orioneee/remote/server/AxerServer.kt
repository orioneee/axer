package io.github.orioneee.remote.server

import io.github.orioneee.Axer
import io.github.orioneee.axer.generated.resources.Res
import io.github.orioneee.axer.generated.resources.axer_already_running
import io.github.orioneee.axer.generated.resources.axer_port_in_use
import io.github.orioneee.axer.generated.resources.server_failed
import io.github.orioneee.axer.generated.resources.server_started
import io.github.orioneee.axer.generated.resources.server_stopped
import io.github.orioneee.domain.other.DeviceData
import io.github.orioneee.domain.other.EnabledFeathers
import io.github.orioneee.koin.IsolatedContext
import io.github.orioneee.processors.RoomReader
import io.github.orioneee.room.dao.AxerExceptionDao
import io.github.orioneee.room.dao.LogsDAO
import io.github.orioneee.room.dao.RequestDao
import io.github.orioneee.storage.AxerSettings
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.cio.CIOApplicationEngine
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.getString
import java.net.InetAddress
import java.net.NetworkInterface
import kotlin.time.Duration.Companion.seconds

internal fun getLocalIpAddress(): String? {
    return NetworkInterface.getNetworkInterfaces().toList().flatMap { it.inetAddresses.toList() }
        .firstOrNull {
            !it.isLoopbackAddress && it is InetAddress && it.hostAddress.indexOf(':') < 0
        }?.hostAddress
}

internal var serverJob: Job? = null

internal expect fun serverNotify(message: String)

fun isPortInUse(port: Int): Boolean {
    return try {
        val socket = java.net.ServerSocket(port)
        socket.close()
        false // port is free
    } catch (e: Exception) {
        true // port is in use
    }
}


suspend fun checkIfAnotherAxerInstanceIsRunning(port: Int): Pair<Boolean, String?> {
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

expect suspend fun sendNotificationAboutRunningServer(
    ip: String,
    port: Int,
    isRunning: Boolean
)

fun Axer.runServerIfNotRunning(scope: CoroutineScope, port: Int = AXER_SERVER_PORT, readOnly: Boolean = false) {
    if (serverJob == null || serverJob?.isCompleted == true) {
        serverJob = scope.launch(SupervisorJob() + Dispatchers.IO) {
            val canRun = runChecksBeforeStartingServer(port)
            if (!canRun) return@launch

            val ip = getLocalIpAddress() ?: "localhost"
            var server: EmbeddedServer<CIOApplicationEngine, CIOApplicationEngine.Configuration>? =
                null
            try {
                coroutineScope {
                    val startedMsg = getString(Res.string.server_started, "$ip:$port")
                    server = getKtorServer(port, readOnly)
                    serverNotify(startedMsg)
                    server.start(wait = true)
                }
            } catch (e: CancellationException) {
                val stoppedMsg = getString(Res.string.server_stopped)
                serverNotify(stoppedMsg)
                server?.stop(1000, 1000)
                throw e
            } catch (e: Exception) {
                val failedMsg = getString(Res.string.server_failed, e.message ?: "unknown error")
                serverNotify(failedMsg)
                server?.stop()
                e.printStackTrace()
            } finally {
                server?.stop(1000, 1000)
            }
        }
    }
}

fun Axer.stopServerIfRunning() {
    if (serverJob != null && serverJob?.isActive == true) {
        serverJob?.cancel()
        serverJob = null
        serverNotify("Axer server stopped")
    } else {
        serverNotify("Axer server is not running")
    }
}

suspend fun runChecksBeforeStartingServer(port: Int): Boolean {
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


const val AXER_SERVER_PORT = 53214

@OptIn(FlowPreview::class)
internal fun CoroutineScope.getKtorServer(
    port: Int,
    readOnly: Boolean
): EmbeddedServer<CIOApplicationEngine, CIOApplicationEngine.Configuration> {
    val reader = RoomReader()
    val requestDao: RequestDao by IsolatedContext.koin.inject()
    val exceptionsDao: AxerExceptionDao by IsolatedContext.koin.inject()
    val logDao: LogsDAO by IsolatedContext.koin.inject()

    val isEnabledRequests = AxerSettings.enableRequestMonitor.asFlow()
    val isEnabledExceptions = AxerSettings.enableExceptionMonitor.asFlow()
    val isEnabledLogs = AxerSettings.enableLogMonitor.asFlow()
    val isEnabledDatabase = AxerSettings.enableDatabaseMonitor.asFlow()

    return embeddedServer(CIO, port = port) {
        install(WebSockets) {
            pingPeriod = 15.seconds
            timeout = 15.seconds
            maxFrameSize = Long.MAX_VALUE
            masking = false
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }
        install(ContentNegotiation) {
            json(Json { prettyPrint = true; isLenient = true })
        }
        install(DefaultHeaders) {
            header("Content-Type", "application/json")
        }

        routing {
            requestsModule(
                isEnabledRequests = isEnabledRequests,
                requestsDao = requestDao,
                readOnly = readOnly,
            )
            exceptionsModule(
                isEnabledExceptions = isEnabledExceptions,
                exceptionsDao = exceptionsDao,
                readOnly = readOnly,
            )
            logsModule(
                isEnabledLogs = isEnabledLogs,
                logsDao = logDao,
                readOnly = readOnly,
            )
            databaseModule(
                isEnabledDatabase = isEnabledDatabase,
                reader = reader,
                readOnly = readOnly,
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
            webSocket("/ws/feathers") {
                fun getEntity(
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

                sendSerialized(
                    getEntity(
                        requests = isEnabledRequests.first(),
                        exceptions = isEnabledExceptions.first(),
                        logs = isEnabledLogs.first(),
                        database = isEnabledDatabase.first(),
                        isReadOnly = readOnly
                    )
                )

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

                for (frame in incoming) {
                }
            }
        }
    }
}