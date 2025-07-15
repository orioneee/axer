package io.github.orioneee.remote.server

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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.net.InetAddress
import java.net.NetworkInterface
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
        routing {
            webSocket("/ws/requests") {
                sendSerialized(requestDao.getAllSync())
                launch {
                    requestDao.getAll().collect { sendSerialized(it) }
                }
                for (frame in incoming) {
                }
            }
            delete("requests") {
                requestDao.deleteAll()
            }
            post("/requests/viewed/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id != null) {
                    requestDao.updateViewed(id.toLong(), true)
                    call.respond(HttpStatusCode.OK, "Request $id marked as viewed")
                } else {
                    call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                }
            }

            webSocket("/ws/requests/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id != null) {
                    val requestFlow = requestDao.getById(id.toLong())
                    sendSerialized(requestFlow)
                    launch {
                        requestFlow.collect { sendSerialized(it) }
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
                    .map {
                        reader.getTablesFromAllDatabase()
                    }
                launch {
                    flow.collect { sendSerialized(it) }
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