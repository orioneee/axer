package io.github.orioneee.remote.server

import io.github.orioneee.Axer
import io.github.orioneee.domain.database.DatabaseData
import io.github.orioneee.domain.database.EditableRowItem
import io.github.orioneee.domain.database.RowItem
import io.github.orioneee.domain.other.EnabledFeathers
import io.github.orioneee.koin.IsolatedContext
import io.github.orioneee.presentation.screens.database.TableDetailsViewModel
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
import io.ktor.websocket.CloseReason
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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

fun Axer.runServerIfNotRunning() {
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

    val isEnabledRequests = AxerSettings.enableRequestMonitor.asFlow()
    val isEnabledExceptions = AxerSettings.enableExceptionMonitor.asFlow()
    val isEnabledLogs = AxerSettings.enableLogMonitor.asFlow()
    val isEnabledDatabase = AxerSettings.enableDatabaseMonitor.asFlow()

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
                if (isEnabledRequests.first()) {
                    requestDao.deleteAll()
                    call.respond(HttpStatusCode.OK, "All requests deleted")
                } else {
                    call.respond(HttpStatusCode.BadRequest, "Request monitoring is disabled")
                }
            }
            post("/requests/view/{id}") {
                if (isEnabledRequests.first()) {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id != null) {
                        requestDao.markAsViewed(id.toLong())
                        call.respond(HttpStatusCode.OK, "Request $id marked as viewed")
                    } else {
                        call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                    }
                } else {
                    call.respond(HttpStatusCode.BadRequest, "Request monitoring is disabled")
                }
            }

            webSocket("/ws/requests/{id}") {
                val id = call.parameters["id"]?.toLongOrNull()
                if (id != null) {
                    if (isEnabledRequests.first()) {
                        sendSerialized(requestDao.getByIdSync(id))
                    }
                    launch {
                        combine(
                            requestDao.getById(id),
                            isEnabledRequests
                        ) { request, isEnabled ->
                            isEnabled.to(request)
                        }.collect {
                            println("Sending request with ID $id to client")
                            if (it.first) {
                                sendSerialized(it.second)
                            }
                        }

                    }

                    for (frame in incoming) {
                    }

                } else {
                    call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                }
            }

            webSocket("/ws/exceptions") {
                if (isEnabledExceptions.first()) {
                    sendSerialized(exceptionsDao.getAllSuspend())
                }
                launch {
                    combine(
                        exceptionsDao.getAll(),
                        isEnabledExceptions
                    ) { exceptions, isEnabled ->
                        isEnabled to exceptions
                    }.collect { (isEnabled, exceptions) ->
                        if (isEnabled) {
                            println("Sending exceptions to client")
                            sendSerialized(exceptions)
                        }
                    }
                }
                for (frame in incoming) {
                }
            }

            delete("exceptions") {
                if (isEnabledExceptions.first()) {
                    exceptionsDao.deleteAll()
                    call.respond(HttpStatusCode.OK, "All exceptions deleted")
                } else {
                    call.respond(HttpStatusCode.BadRequest, "Exception monitoring is disabled")
                }
            }

            webSocket("/ws/requests") {
                if (isEnabledRequests.first()) {
                    sendSerialized(requestDao.getAllSync())
                }
                launch {
                    combine(
                        requestDao.getAll(),
                        isEnabledRequests
                    ) { requests, isEnabled ->
                        isEnabled to requests
                    }.collect { (isEnabled, requests) ->
                        if (isEnabled) {
                            println("Sending requests to client")
                            sendSerialized(requests)
                        }
                    }
                }
                for (frame in incoming) {
                }
            }
            webSocket("/ws/exceptions/{id}") {
                val id = call.parameters["id"]?.toLongOrNull()
                if (id != null) {
                    if (isEnabledExceptions.first()) {
                        sendSerialized(exceptionsDao.getByIDSync(id))
                    }
                    launch {
                        combine(
                            exceptionsDao.getByID(id),
                            isEnabledExceptions
                        ) { exception, isEnabled ->
                            isEnabled to exception
                        }.collect { (isEnabled, exception) ->
                            if (isEnabled) {
                                println("Sending exception with ID $id to client")
                                sendSerialized(exception)
                            }
                        }
                    }
                    for (frame in incoming) {
                    }
                } else {
                    call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                }
            }

            webSocket("/ws/logs") {
                if (isEnabledLogs.first()) {
                    sendSerialized(logDao.getAllSync())
                }
                launch {
                    combine(
                        logDao.getAll(),
                        isEnabledLogs
                    ) { logs, isEnabled ->
                        isEnabled to logs
                    }.collect { (isEnabled, logs) ->
                        if (isEnabled) {
                            println("Sending logs to client")
                            sendSerialized(logs)
                        }
                    }
                }
                for (frame in incoming) {
                }
            }

            delete("logs") {
                if (isEnabledLogs.first()) {
                    logDao.clear()
                    call.respond(HttpStatusCode.OK, "All logs deleted")
                } else {
                    call.respond(HttpStatusCode.BadRequest, "Log monitoring is disabled")
                }
            }

            webSocket("/ws/database") {
                if (isEnabledDatabase.first()) {
                    val tables = dbMutex.withLock {
                        reader.getTablesFromAllDatabase()
                    }
                    sendSerialized(tables)
                }
                reader.axerDriver.changeDataFlow
                    .debounce(100)
                    .onEach {
                        if (isEnabledDatabase.first()) {
                            val tables = dbMutex.withLock {
                                reader.getTablesFromAllDatabase()
                            }
                            sendSerialized(tables)
                        }
                    }
                    .launchIn(this)
                for (frame in incoming) {
                }
            }

            post("database/cell/{file}/{table}") {
                if (isEnabledDatabase.first()) {
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
                } else {
                    call.respond(HttpStatusCode.BadRequest, "Database monitoring is disabled")
                }
            }

            delete("database/row/{file}/{table}") {
                if (isEnabledDatabase.first()) {
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

                if (isEnabledDatabase.first()) {
                    sendSerialized(getTableInfo())
                }
                reader.axerDriver.changeDataFlow
                    .debounce(100)
                    .onEach {
                        if (isEnabledDatabase.first()) {
                            sendSerialized(getTableInfo())
                        }
                    }
                    .launchIn(this)
                for (frame in incoming) {
                }
            }

            webSocket("/ws/db_queries") {
                reader.axerDriver.allQueryFlow
                    .onEach {
                        if (isEnabledDatabase.first()) {
                            sendSerialized(it)
                        }
                    }
                    .launchIn(this)
                for (frame in incoming) {
                }
            }
            post("/ws/db_queries/execute/{file}") {
                if (isEnabledDatabase.first()) {
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
                } else {
                    call.respond(HttpStatusCode.BadRequest, "Database monitoring is disabled")
                }
            }

            webSocket("/ws/db_queries/execute_and_get_updates/{file}") {
                val file = call.parameters["file"]
                if (file == null) {
                    close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "File parameter is missing"))
                    return@webSocket
                }

                var command: String? = null
                reader.axerDriver.changeDataFlow
                    .debounce(100)
                    .onEach {
                        command?.let {
                            if (isEnabledDatabase.first()) {
                                val response = dbMutex.withLock {
                                    reader.executeRawQuery(
                                        file = file,
                                        query = it
                                    )
                                }
                                sendSerialized(response)
                            }
                        }
                    }
                    .launchIn(this)
                for (frame in incoming) {
                    if (command == null && frame is io.ktor.websocket.Frame.Text) {
                        val text = frame.readText()
                        if (text.isNotBlank()) {
                            command = text
                            if (isEnabledDatabase.first()) {
                                val response = dbMutex.withLock {
                                    reader.executeRawQuery(
                                        file = file,
                                        query = text
                                    )
                                }
                                sendSerialized(response)
                            }
                        }
                    } else if (frame is io.ktor.websocket.Frame.Close) {
                        println("WebSocket connection closed")
                        break
                    }
                }
            }


            delete("/database/{file}/{table}") {
                if (isEnabledDatabase.first()) {
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
                } else {
                    call.respond(HttpStatusCode.BadRequest, "Database monitoring is disabled")
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
    }.start(wait = false)
}