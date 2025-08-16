package io.github.orioneee.internal.remote.server

import io.github.orioneee.internal.domain.other.BaseResponse
import io.github.orioneee.internal.room.dao.LogsDAO
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

internal fun Route.logsModule(
    logsDao: LogsDAO,
    isEnabledLogs: Flow<Boolean>,
    readOnly: Boolean,
    onAddClient: (ApplicationCall) -> Unit,
    onRemoveClient: (ApplicationCall) -> Unit
) {
    reactiveUpdatesSocket(
        path = "/ws/logs",
        isEnabledFlow = { isEnabledLogs },
        initialData = { logsDao.getAllSync() },
        dataFlow = { logsDao.getAll() },
        getId = { it.id },
        chunkSize = 5000,
        onAddClient = onAddClient,
        onRemoveClient = onRemoveClient
    )


    delete("logs") {
        if (readOnly) {
            call.respond(
                HttpStatusCode.MethodNotAllowed,
                BaseResponse<String>(
                    status = HttpStatusCode.MethodNotAllowed.description,
                    error = "Logs deletion is not allowed in read-only mode"
                )
            )
            return@delete
        }
        if (isEnabledLogs.first()) {
            logsDao.clear()
            call.respond(
                HttpStatusCode.OK,
                BaseResponse(
                    status = HttpStatusCode.OK.description,
                    data = "Logs cleared successfully"
                )
            )
        } else {
            call.respond(
                HttpStatusCode.BadRequest,
                BaseResponse<String>(
                    status = HttpStatusCode.BadRequest.description,
                    error = "Logs monitoring is disabled"
                )
            )
        }
    }
}