package io.github.orioneee.remote.server

import io.github.orioneee.domain.database.DatabaseData
import io.github.orioneee.domain.database.EditableRowItem
import io.github.orioneee.domain.database.RowItem
import io.github.orioneee.presentation.screens.database.TableDetailsViewModel
import io.github.orioneee.processors.RoomReader
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.post
import io.ktor.server.websocket.sendSerialized
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@OptIn(FlowPreview::class)
internal fun Route.databaseModule(
    isEnabledDatabase: Flow<Boolean>,
    reader: RoomReader,
) {
    val dbMutex = Mutex()
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
            )
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
}