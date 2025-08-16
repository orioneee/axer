package io.github.orioneee.internal.remote.server

import io.github.orioneee.internal.domain.database.DatabaseData
import io.github.orioneee.internal.domain.database.EditableRowItem
import io.github.orioneee.internal.domain.database.RowItem
import io.github.orioneee.internal.domain.other.BaseResponse
import io.github.orioneee.internal.presentation.screens.database.TableDetailsViewModel
import io.github.orioneee.internal.processors.RoomReader
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.post
import io.ktor.server.websocket.sendSerialized
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@OptIn(FlowPreview::class)
internal fun Route.databaseModule(
    isEnabledDatabase: Flow<Boolean>,
    reader: RoomReader,
    readOnly: Boolean,
    onAddClient: (ApplicationCall) -> Unit,
    onRemoveClient: (ApplicationCall) -> Unit
) {
    val dbMutex = Mutex()
    webSocket("/ws/database") {
        if (isEnabledDatabase.first()) {
            val tables = dbMutex.withLock {
                reader.getTablesFromAllDatabase()
            }
            sendSerialized(tables)
            onAddClient(call)
        } else {
            sendSerialized(null)
        }
        reader.axerDriver.changeDataFlow
            .onEach {
                if (isEnabledDatabase.first()) {
                    val tables = dbMutex.withLock {
                        reader.getTablesFromAllDatabase()
                    }
                    sendSerialized(tables)
                }
            }
            .launchIn(this)
        try {
            for (frame in incoming) {
            }
        } catch (e: Exception) {
            e.printStackTrace()
            close(CloseReason(CloseReason.Codes.INTERNAL_ERROR, e.stackTraceToString()))
        } finally {
            onRemoveClient(call)
        }
    }

    post("database/cell/{file}/{table}") {
        if (readOnly) {
            call.respond<BaseResponse<String>>(
                HttpStatusCode.MethodNotAllowed,
                BaseResponse(
                    status = HttpStatusCode.MethodNotAllowed.description,
                    error = "Database updates are not allowed in read-only mode"
                )
            )

            return@post
        }
        if (isEnabledDatabase.first()) {
            val file = call.parameters["file"]
            val tableName = call.parameters["table"]
            val body: EditableRowItem = call.receive()
            if (file == null || tableName == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    BaseResponse<String>(
                        status = HttpStatusCode.BadRequest.description,
                        error = "File or table name is missing"
                    )
                )
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
                call.respond(
                    HttpStatusCode.OK,
                    BaseResponse(
                        status = HttpStatusCode.OK.description,
                        data = "Cell updated successfully"
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    BaseResponse<String>(
                        status = HttpStatusCode.InternalServerError.description,
                        error = "Error updating cell: ${e.message}"
                    )
                )
            }
        } else {
            call.respond(
                HttpStatusCode.BadRequest,
                BaseResponse<String>(
                    status = HttpStatusCode.BadRequest.description,
                    error = "Database monitoring is disabled"
                )
            )
        }
    }

    delete("database/row/{file}/{table}") {
        if (readOnly) {
            call.respond(
                HttpStatusCode.MethodNotAllowed,
                BaseResponse<String>(
                    status = HttpStatusCode.MethodNotAllowed.description,
                    error = "Database updates are not allowed in read-only mode"
                )
            )
            return@delete
        }
        if (isEnabledDatabase.first()) {
            val file = call.parameters["file"]
            val tableName = call.parameters["table"]
            val body: RowItem = call.receive()
            if (file == null || tableName == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    BaseResponse<String>(
                        status = HttpStatusCode.BadRequest.description,
                        error = "File or table name is missing"
                    )
                )
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
                call.respond(
                    HttpStatusCode.OK,
                    BaseResponse(
                        status = HttpStatusCode.OK.description,
                        data = "Row deleted successfully"
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    BaseResponse<String>(
                        status = HttpStatusCode.InternalServerError.description,
                        error = "Error deleting row: ${e.message}"
                    )
                )
            }
        }
    }


    webSocket("/ws/database/{file}/{table}/{page}") {
        val file = call.parameters["file"] ?: return@webSocket
        val table = call.parameters["table"] ?: return@webSocket
        val page = call.parameters["page"]?.toIntOrNull() ?: 0
        val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: TableDetailsViewModel.PAGE_SIZE

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

        try {
            if (isEnabledDatabase.first()) {
                sendSerialized(getTableInfo())
                onAddClient(call)
            } else {
                sendSerialized(null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            close(CloseReason(CloseReason.Codes.INTERNAL_ERROR, e.stackTraceToString()))
        }
        reader.axerDriver.changeDataFlow
            .onEach {
                if (isEnabledDatabase.first()) {
                    try {
                        sendSerialized(getTableInfo())
                    } catch (e: Exception) {
                        e.printStackTrace()
                        close(CloseReason(CloseReason.Codes.INTERNAL_ERROR, e.stackTraceToString()))
                    }
                }
            }
            .launchIn(this)
        try{
            for (frame in incoming) {
            }
        } catch (e: Exception) {
            e.printStackTrace()
            close(CloseReason(CloseReason.Codes.INTERNAL_ERROR, e.stackTraceToString()))
        } finally {
            onRemoveClient(call)
        }
    }

    webSocket("/ws/db_queries") {
        onAddClient(call)
        reader.axerDriver.allQueryFlow
            .onEach {
                if (isEnabledDatabase.first()) {
                    sendSerialized(it)
                }
            }
            .launchIn(this)
        try {
            for (frame in incoming) {
            }
        } catch (e: Exception) {
            e.printStackTrace()
            close(CloseReason(CloseReason.Codes.INTERNAL_ERROR, e.stackTraceToString()))
        } finally {
            onRemoveClient(call)
        }
    }
    post("/ws/db_queries/execute/{file}") {
        if (readOnly) {
            call.respond(
                HttpStatusCode.MethodNotAllowed,
                BaseResponse<String>(
                    status = HttpStatusCode.MethodNotAllowed.description,
                    error = "Database updates are not allowed in read-only mode"
                )
            )
            return@post
        }
        if (isEnabledDatabase.first()) {
            val file = call.parameters["file"]
            if (file == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    BaseResponse<String>(
                        status = HttpStatusCode.BadRequest.description,
                        error = "File parameter is missing"
                    )
                )
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
                call.respond(
                    HttpStatusCode.OK,
                    BaseResponse(
                        status = HttpStatusCode.OK.description,
                        data = "Query executed successfully"
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    BaseResponse<String>(
                        status = HttpStatusCode.InternalServerError.description,
                        error = "Error executing query: ${e.message}"
                    )
                )
            }
        } else {
            call.respond(
                HttpStatusCode.BadRequest,
                BaseResponse<String>(
                    status = HttpStatusCode.BadRequest.description,
                    error = "Database monitoring is disabled"
                )
            )
        }
    }

    webSocket("/ws/db_queries/execute_and_get_updates/{file}") {
        val file = call.parameters["file"]
        if (file == null) {
            close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "File parameter is missing"))
            return@webSocket
        }
        onAddClient(call)
        var command: String? = null
        reader.axerDriver.changeDataFlow
            .onEach {
                command?.let {
                    val isViewCommand = it.trimStart()
                        .uppercase()
                        .startsWith("SELECT") || it.uppercase().startsWith("PRAGMA")
                    if (!isViewCommand && readOnly) {
                        close(
                            CloseReason(
                                CloseReason.Codes.CANNOT_ACCEPT,
                                "Database updates are not allowed in read-only mode"
                            )
                        )
                        return@onEach
                    }

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
        try{
            for (frame in incoming) {
                if (command == null && frame is Frame.Text) {
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
                } else if (frame is Frame.Close) {
                    break
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            close(CloseReason(CloseReason.Codes.INTERNAL_ERROR, e.stackTraceToString()))
        } finally {
            onRemoveClient(call)
        }
    }


    delete("/database/{file}/{table}") {
        if (readOnly) {
            call.respond(
                HttpStatusCode.MethodNotAllowed,
                BaseResponse<String>(
                    status = HttpStatusCode.MethodNotAllowed.description,
                    error = "Database updates are not allowed in read-only mode"
                )
            )
            return@delete
        }
        if (isEnabledDatabase.first()) {
            val file = call.parameters["file"] ?: return@delete
            val table = call.parameters["table"] ?: return@delete
            try {
                dbMutex.withLock {
                    reader.clearTable(file, table)
                }
                call.respond(
                    HttpStatusCode.OK,
                    BaseResponse(
                        status = HttpStatusCode.OK.description,
                        data = "Table cleared successfully"
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    BaseResponse<String>(
                        status = HttpStatusCode.InternalServerError.description,
                        error = "Error clearing table: ${e.message}"
                    )
                )
            }
        } else {
            call.respond(
                HttpStatusCode.BadRequest,
                BaseResponse<String>(
                    status = HttpStatusCode.BadRequest.description,
                    error = "Database monitoring is disabled"
                )
            )
        }
    }
}