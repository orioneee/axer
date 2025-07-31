package io.github.orioneee.remote.server

import io.github.orioneee.Axer
import io.github.orioneee.domain.other.EnabledFeathers
import io.github.orioneee.koin.IsolatedContext
import io.github.orioneee.processors.RoomReader
import io.github.orioneee.room.dao.AxerExceptionDao
import io.github.orioneee.room.dao.LogsDAO
import io.github.orioneee.room.dao.RequestDao
import io.github.orioneee.storage.AxerSettings
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
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

fun Axer.runServerIfNotRunning(scope: CoroutineScope, port: Int = AXER_SERVER_PORT) {
    if (serverJob == null || serverJob?.isCompleted == true) {
        serverJob = scope.launch(Dispatchers.IO) {
            var server:  EmbeddedServer<CIOApplicationEngine, CIOApplicationEngine.Configuration>? = null
            try {
                server = getKtorServer(port)
                serverNotify("Axer server started on ${getLocalIpAddress() ?: "localhost"}:$port")
                server.start(wait = true)
            } catch (e: Throwable) {
                serverNotify("Axer server stopped")
            } catch (e: Exception) {
                serverNotify("Axer server failed: ${e.message}")
                server?.stop()
                e.printStackTrace()
            }
        }
    }
}

const val AXER_SERVER_PORT = 53214

@OptIn(FlowPreview::class)
private fun CoroutineScope.getKtorServer(
    port: Int
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
            )
            exceptionsModule(
                isEnabledExceptions = isEnabledExceptions,
                exceptionsDao = exceptionsDao,
            )
            logsModule(
                isEnabledLogs = isEnabledLogs,
                logsDao = logDao,
            )
            databaseModule(
                isEnabledDatabase = isEnabledDatabase,
                reader = reader,
            )

            get("/isAxerServer") {
                try {
                    call.respond(HttpStatusCode.OK, getDeviceData())
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
                    database: Boolean
                ): EnabledFeathers {
                    return EnabledFeathers(
                        isEnabledRequests = requests,
                        isEnabledExceptions = exceptions,
                        isEnabledLogs = logs,
                        isEnabledDatabase = database
                    )
                }

                sendSerialized(
                    getEntity(
                        requests = isEnabledRequests.first(),
                        exceptions = isEnabledExceptions.first(),
                        logs = isEnabledLogs.first(),
                        database = isEnabledDatabase.first()
                    )
                )

                launch {
                    combine(
                        isEnabledRequests,
                        isEnabledExceptions,
                        isEnabledLogs,
                        isEnabledDatabase
                    ) { requests, exceptions, logs, database ->
                        getEntity(requests, exceptions, logs, database)
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