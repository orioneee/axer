package io.github.orioneee.remote.server

import io.github.orioneee.domain.other.BaseResponse
import io.github.orioneee.domain.requests.data.TransactionFull
import io.github.orioneee.room.dao.RequestDao
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.websocket.sendSerialized
import io.ktor.server.websocket.webSocket
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds


@OptIn(FlowPreview::class)
internal fun Route.requestsModule(
    isEnabledRequests: Flow<Boolean>,
    requestsDao: RequestDao,
    readOnly: Boolean,
) {
    reactiveUpdatesSocket(
        path = "/ws/requests",
        isEnabledFlow = { isEnabledRequests },
        initialData = { requestsDao.getAllShortSync() },
        dataFlow = { requestsDao.getAllShort() },
        getId = { it.id },
    )

    webSocket("/ws/requests/{id}") {
        val id = call.parameters["id"]?.toLongOrNull()
        if (id != null) {
            if (isEnabledRequests.first()) {
                sendSerialized(requestsDao.getByIdSync(id))
            } else {
                sendSerialized(null)
            }
            launch {
                combine(
                    requestsDao.getById(id)
                        .distinctUntilChanged()
                        .debounce(500.milliseconds),
                    isEnabledRequests
                ) { request, isEnabled ->
                    isEnabled.to(request)
                }.debounce(500.milliseconds)
                    .collect {
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

    get("requests/full") {
        if (isEnabledRequests.first()) {
            val requests = requestsDao.getAllSync()
            val response = BaseResponse(
                status = HttpStatusCode.OK.description,
                data = requests
            )
            call.respond(HttpStatusCode.OK, response)
        } else {
            call.respond(
                HttpStatusCode.BadRequest,
                BaseResponse<List<TransactionFull>>(
                    status = HttpStatusCode.BadRequest.description,
                    error = "Request monitoring is disabled"
                )
            )
        }
    }


    delete("requests") {
        if (readOnly) {
            call.respond(
                HttpStatusCode.MethodNotAllowed,
                BaseResponse<String>(
                    status = HttpStatusCode.MethodNotAllowed.description,
                    error = "Requests deletion is not allowed in read-only mode"
                )
            )
            return@delete
        }
        if (isEnabledRequests.first()) {
            requestsDao.deleteAll()
            call.respond(HttpStatusCode.OK,
                BaseResponse(
                    status = HttpStatusCode.OK.description,
                    data = "Requests cleared successfully"
                )
            )
        } else {
            call.respond(
                HttpStatusCode.BadRequest,
                BaseResponse<String>(
                    status = HttpStatusCode.BadRequest.description,
                    error = "Request monitoring is disabled"
                )
            )
        }
    }
    post("/requests/view/{id}") {
        if (isEnabledRequests.first()) {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id != null) {
                requestsDao.markAsViewed(id.toLong())
                call.respond(HttpStatusCode.OK,
                    BaseResponse(
                        status = HttpStatusCode.OK.description,
                        data = "Request with ID $id marked as viewed"
                    )
                )
            } else {
                call.respond(HttpStatusCode.BadRequest,
                    BaseResponse<String>(
                        status = HttpStatusCode.BadRequest.description,
                        error = "Invalid request ID"
                    )
                )
            }
        } else {
            call.respond(
                HttpStatusCode.BadRequest,
                BaseResponse<String>(
                    status = HttpStatusCode.BadRequest.description,
                    error = "Request monitoring is disabled"
                )
            )
        }
    }
}