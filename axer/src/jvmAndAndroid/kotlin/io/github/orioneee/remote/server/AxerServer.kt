package io.github.orioneee.remote.server

import io.github.orioneee.Axer
import io.github.orioneee.koin.IsolatedContext
import io.github.orioneee.processors.RoomReader
import io.github.orioneee.room.dao.AxerExceptionDao
import io.github.orioneee.room.dao.LogsDAO
import io.github.orioneee.room.dao.RequestDao
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.sendSerialized
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.DefaultWebSocketSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.Collections
import kotlin.time.Duration.Companion.seconds

fun getLocalIpAddress(): String? {
    return NetworkInterface.getNetworkInterfaces().toList().flatMap { it.inetAddresses.toList() }
        .firstOrNull {
            !it.isLoopbackAddress && it is InetAddress && it.hostAddress.indexOf(':') < 0
        }?.hostAddress
}

var serverJob: Job? = null

fun runServerIfNotRunning() {
    if (serverJob == null || serverJob?.isCompleted == true) {
        serverJob = CoroutineScope(Dispatchers.IO).launch {
            startKtorServer(9000)
        }
    }
}

@OptIn(FlowPreview::class)
private fun CoroutineScope.startKtorServer(
    port: Int = 9000
) {
    val localIp = getLocalIpAddress() ?: "localhost"
    println("Local IP address: $localIp")
    println("Starting Ktor server on port $port")
    val requestDao: RequestDao by IsolatedContext.koin.inject()
    val exceptionsDao: AxerExceptionDao by IsolatedContext.koin.inject()
    val logDao: LogsDAO by IsolatedContext.koin.inject()
    val reader = RoomReader()
    embeddedServer(CIO, port = port) {
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
        val activeConnections = Collections.synchronizedSet<DefaultWebSocketSession>(mutableSetOf())
        routing {

            delete("requests") {
                requestDao.deleteAll()
                call.respond(HttpStatusCode.OK, "All requests deleted")
            }
            post("/requests/view/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id != null) {
                    requestDao.markAsViewed(id.toLong())
                    call.respond(HttpStatusCode.OK, "Request $id marked as viewed")
                } else {
                    call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                }
            }

            webSocket("/ws/requests/{id}") {
                println("WebSocket connection established for request with ID: ${call.parameters["id"]}")
                val id = call.parameters["id"]?.toLongOrNull()
                if (id != null) {
                    sendSerialized(requestDao.getByIdSync(id))
                    launch {
                        requestDao.getById(id)
                            .collect {
                                println("Sending request with ID $id to client")
                                sendSerialized(it)
                            }
                    }

                    for (frame in incoming) {
                    }

                } else {
                    call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                }
            }

            webSocket("/ws/exceptions") {
                sendSerialized(exceptionsDao.getAllSuspend())
                launch {
                    exceptionsDao.getAll().collect { sendSerialized(it) }
                }
                for (frame in incoming) {
                }
            }

            delete("exceptions") {
                exceptionsDao.deleteAll()
                call.respond(HttpStatusCode.OK, "All exceptions deleted")
            }

            webSocket("/ws/requests") {
                sendSerialized(requestDao.getAllSync())
                launch {
                    requestDao.getAll().collect { sendSerialized(it) }
                }
                for (frame in incoming) {
                }
            }
            webSocket("/ws/exceptions/{id}") {
                val id = call.parameters["id"]?.toLongOrNull()
                if (id != null) {
                    sendSerialized(exceptionsDao.getByIDSync(id))
                    launch {
                        exceptionsDao.getByID(id).collect { sendSerialized(it) }
                    }
                    for (frame in incoming) {
                    }
                } else {
                    call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                }
            }

            webSocket("/ws/logs") {
                sendSerialized(logDao.getAllSync())
                launch {
                    logDao.getAll().collect { sendSerialized(it) }
                }
                for (frame in incoming) {
                }
            }

            delete("logs") {
                logDao.clear()
                call.respond(HttpStatusCode.OK, "All logs deleted")
            }

            webSocket("/ws/database") {
                val tables = reader.getTablesFromAllDatabase()
                sendSerialized(tables)
                val flow = reader.axerDriver.changeDataFlow
                    .debounce(100)
                    .onEach {
                        reader.getTablesFromAllDatabase()
                    }
                launch {
                    flow.collect {
                        sendSerialized(it)
                    }
                }
                for (frame in incoming) {
                }
            }

            post("/query") {
                val body = call.receive<String>()
                println("Received query: $body")
                call.respond(HttpStatusCode.OK, "Query received: $body")
            }
        }
    }.start(wait = false)
}