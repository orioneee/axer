package io.github.orioneee.internal.remote.server

import io.github.orioneee.internal.domain.exceptions.SessionException
import io.github.orioneee.internal.domain.other.BaseResponse
import io.github.orioneee.internal.room.dao.AxerExceptionDao
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

internal fun Route.exceptionsModule(
    exceptionsDao: AxerExceptionDao,
    isEnabledExceptions: Flow<Boolean>,
    readOnly: Boolean,
    onAddClient: (ApplicationCall) -> Unit,
    onRemoveClient: (ApplicationCall) -> Unit
) {
    reactiveUpdatesSocket(
        path = "/ws/exceptions",
        isEnabledFlow = { isEnabledExceptions },
        initialData = { exceptionsDao.getAllSuspend() },
        dataFlow = { exceptionsDao.getAll() },
        getId = { it.id },
        onAddClient = onAddClient,
        onRemoveClient = onRemoveClient
    )


    delete("exceptions") {
        if (readOnly) {
            call.respond(
                HttpStatusCode.MethodNotAllowed,
                BaseResponse<String>(
                    status = HttpStatusCode.MethodNotAllowed.description,
                    error = "Exceptions deletion is not allowed in read-only mode"
                )
            )
            return@delete
        }
        if (isEnabledExceptions.first()) {
            exceptionsDao.deleteAll()
            call.respond(
                HttpStatusCode.OK,
                BaseResponse(
                    status = HttpStatusCode.OK.description,
                    data = "Exceptions cleared successfully"
                )
            )
        } else {
            call.respond(
                HttpStatusCode.BadRequest,
                BaseResponse<String>(
                    status = HttpStatusCode.BadRequest.description,
                    error = "Exception monitoring is disabled"
                )
            )
        }
    }
    get("/exceptions/{id}") {
        try {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id != null) {
                if (isEnabledExceptions.first()) {
                    val data = exceptionsDao.getSessionEvents(id)
                    if (data != null) {
                        val response = BaseResponse(
                            status = HttpStatusCode.OK.description,
                            data = data
                        )
                        call.respond(
                            HttpStatusCode.OK,
                            response
                        )
                    } else {
                        call.respond(
                            HttpStatusCode.NotFound,
                            BaseResponse<SessionException>(
                                status = HttpStatusCode.NotFound.description,
                                error = "Exception with ID $id not found"
                            )
                        )
                    }
                } else {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        BaseResponse<SessionException>(
                            status = HttpStatusCode.BadRequest.description,
                            error = "Exception monitoring is disabled"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            call.respond(
                HttpStatusCode.InternalServerError,
                BaseResponse<SessionException>(
                    status = HttpStatusCode.InternalServerError.description,
                    error = "An error occurred while fetching the exception: ${e.message}"
                )
            )
        }
    }
}