package io.github.orioneee.remote.server

import io.github.orioneee.domain.requests.data.Transaction
import io.github.orioneee.domain.requests.data.TransactionFull
import io.github.orioneee.domain.requests.data.TransactionShort
import io.github.orioneee.room.dao.RequestDao
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.websocket.sendSerialized
import io.ktor.server.websocket.webSocket
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json


internal fun Route.requestsModule(
    isEnabledRequests: Flow<Boolean>,
    requestsDao: RequestDao,
) {
    reactiveUpdatesSocket(
        path = "/ws/requests",
        isEnabledFlow = { isEnabledRequests },
        initialData = { requestsDao.getAllShortSync() },
        dataFlow = { requestsDao.getAllShort() },
        getId = { it.id },
        debounceTimeMillis = 50,
        sendsToReplaceAll = 10,
    )

    webSocket("/ws/requests/{id}") {
        val id = call.parameters["id"]?.toLongOrNull()
        if (id != null) {
            if (isEnabledRequests.first()) {
                sendSerialized(requestsDao.getByIdSync(id))
            } else{
                sendSerialized(null)
            }
            launch {
                combine(
                    requestsDao.getById(id),
                    isEnabledRequests
                ) { request, isEnabled ->
                    isEnabled.to(request)
                }.collect {
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
            call.respond(HttpStatusCode.OK, requests)
        } else {
            call.respond(HttpStatusCode.BadRequest, "Request monitoring is disabled")
        }
    }


    delete("requests") {
        if (isEnabledRequests.first()) {
            requestsDao.deleteAll()
            call.respond(HttpStatusCode.OK, "All requests deleted")
        } else {
            call.respond(HttpStatusCode.BadRequest, "Request monitoring is disabled")
        }
    }
    post("/requests/view/{id}") {
        if (isEnabledRequests.first()) {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id != null) {
                requestsDao.markAsViewed(id.toLong())
                call.respond(HttpStatusCode.OK, "Request $id marked as viewed")
            } else {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID")
            }
        } else {
            call.respond(HttpStatusCode.BadRequest, "Request monitoring is disabled")
        }
    }
}