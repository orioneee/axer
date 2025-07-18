package io.github.orioneee.remote.server

import androidx.lifecycle.viewModelScope
import io.github.orioneee.Axer
import io.github.orioneee.AxerDataProvider
import io.github.orioneee.domain.database.DatabaseData
import io.github.orioneee.domain.database.EditableRowItem
import io.github.orioneee.domain.database.RoomCell
import io.github.orioneee.domain.database.RowItem
import io.github.orioneee.domain.database.SchemaItem
import io.github.orioneee.koin.IsolatedContext
import io.github.orioneee.presentation.screens.database.TableDetailsViewModel
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
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.sendSerialized
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
    val dbMutex = Mutex()
    val reader = RoomReader()
    val localIp = getLocalIpAddress() ?: "localhost"
    println("Local IP address: $localIp")
    println("Starting Ktor server on port $port")
    val requestDao: RequestDao by IsolatedContext.koin.inject()
    val exceptionsDao: AxerExceptionDao by IsolatedContext.koin.inject()
    val logDao: LogsDAO by IsolatedContext.koin.inject()
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
                val tables = dbMutex.withLock {
                    reader.getTablesFromAllDatabase()
                }
                sendSerialized(tables)
                reader.axerDriver.changeDataFlow
                    .debounce(100)
                    .onEach {
                        val tables = dbMutex.withLock {
                            reader.getTablesFromAllDatabase()
                        }
                        sendSerialized(tables)
                    }
                    .launchIn(this)
                for (frame in incoming) {
                }
            }

            post("database/cell/{file}/{table}") {
                val file = call.parameters["file"]
                val tableName = call.parameters["table"]
                val body: EditableRowItem = call.receive()
                if (file == null || tableName == null) {
                    call.respond(HttpStatusCode.BadRequest, "File or table name is missing")
                    return@post
                }
                println("Received update cell request: $body")
                try {
                    dbMutex.withLock {
                        reader.updateCell(
                            file = file,
                            tableName = tableName,
                            editableItem = body
                        )
                    }
                    call.respond(HttpStatusCode.OK, "Cell updated successfully")
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        "Error updating cell: ${e.message}"
                    )
                }
            }

            delete("database/row/{file}/{table}") {
                val file = call.parameters["file"]
                val tableName = call.parameters["table"]
                val body: RowItem = call.receive()
                if (file == null || tableName == null) {
                    call.respond(HttpStatusCode.BadRequest, "File or table name is missing")
                    return@delete
                }
                println("Received delete row request: $body")
                try {
                    dbMutex.withLock {
                        reader.deleteRow(
                            file = file,
                            tableName = tableName,
                            row = body
                        )
                    }
                    call.respond(HttpStatusCode.OK, "Row deleted successfully")
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        "Error deleting row: ${e.message}"
                    )
                }
            }


            webSocket("/ws/database/{file}/{table}/{page}") {
                val file = call.parameters["file"] ?: return@webSocket
                val table = call.parameters["table"] ?: return@webSocket
                val page = call.parameters["page"]?.toIntOrNull() ?: 0
                val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull()
                    ?: TableDetailsViewModel.PAGE_SIZE
                println("WebSocket connection established for file: $file, table: $table, page: $page pageSize: $pageSize")

                suspend fun getTableInfo(): DatabaseData {
                    val content = dbMutex.withLock {
                        reader.getTableContent(
                            file = file,
                            tableName = table,
                            page = page,
                            pageSize = pageSize
                        )
                    }
                    val schema = dbMutex.withLock {
                        reader.getTableSchema(file, table)
                    }
                    val size = dbMutex.withLock {
                        reader.getTableSize(file, table)
                    }
                    return DatabaseData(
                        schema,
                        content,
                        size
                    ).also {
                        println("Try to send ${Json.encodeToString(it)}")
                    }
                }

                sendSerialized(getTableInfo())
                reader.axerDriver.changeDataFlow
                    .debounce(100)
                    .onEach {
                        sendSerialized(getTableInfo())
                    }
                    .launchIn(this)
                for (frame in incoming) {
                }
            }

            webSocket("/ws/db_queries") {
                reader.axerDriver.allQueryFlow
                    .onEach {
                        sendSerialized(it)
                    }
                    .launchIn(this)
                for (frame in incoming) {
                }
            }
            post("/ws/db_queries/execute/{file}") {
                val file = call.parameters["file"]
                if (file == null) {
                    call.respond(HttpStatusCode.BadRequest, "File parameter is missing")
                    return@post
                }
                val body: String = call.receive()
                println("Received query: $body")
                try {
                    dbMutex.withLock {
                        reader.executeRawQuery(
                            file = file,
                            query = body
                        )
                    }
                    call.respond(HttpStatusCode.OK, "Query executed successfully")
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        "Error executing query: ${e.message}"
                    )
                }
            }

            webSocket("/ws/db_queries/execute/{file}") {
                val file = call.parameters["file"]
                if (file == null) {
                    call.respond(HttpStatusCode.BadRequest, "File parameter is missing")
                    return@webSocket
                }
                var command: String? = null
                reader.axerDriver.changeDataFlow
                    .debounce(100)
                    .onEach {
                        command?.let {
                            val response = dbMutex.withLock {
                                reader.executeRawQuery(
                                    file = file,
                                    query = it
                                )
                            }
                            sendSerialized(response)
                        }
                    }
                    .launchIn(this)
                for (frame in incoming) {
                    if (command == null && frame is io.ktor.websocket.Frame.Text) {
                        val text = frame.readText()
                        if (text.isNotBlank()) {
                            command = text
                            val response = dbMutex.withLock {
                                reader.executeRawQuery(
                                    file = file,
                                    query = text
                                )
                            }
                            sendSerialized(response)
                        }
                    } else if (frame is io.ktor.websocket.Frame.Close) {
                        println("WebSocket connection closed")
                        break
                    }
                }
            }


            delete("/database/{file}/{table}") {
                val file = call.parameters["file"] ?: return@delete
                val table = call.parameters["table"] ?: return@delete
                try {
                    dbMutex.withLock {
                        reader.clearTable(file, table)
                    }
                    call.respond(HttpStatusCode.OK, "Table $table in file $file cleared")
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        "Error clearing table: ${e.message}"
                    )
                }
            }
            get("/isAxerServer") {
                call.respond(HttpStatusCode.OK, getDeviceData())
            }

            webSocket("/ws/isAlive") {
                println("WebSocket connection established for isAlive")
                try {
                    while (true) {
                        ensureActive()
                        delay(1000L)
                        val time = System.currentTimeMillis()
                        sendSerialized("ping - $time")
                    }
                } catch (e: Exception) {
                    println("WebSocket connection closed: ${e.message}")
                }
            }
        }
    }.start(wait = false)
}