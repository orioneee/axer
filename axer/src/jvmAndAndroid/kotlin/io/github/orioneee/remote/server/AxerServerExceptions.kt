package io.github.orioneee.remote.server

import io.github.orioneee.room.dao.AxerExceptionDao
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.websocket.sendSerialized
import io.ktor.server.websocket.webSocket
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

internal fun Route.exceptionsModule(
    exceptionsDao: AxerExceptionDao,
    isEnabledExceptions: Flow<Boolean>
) {
    reactiveUpdatesSocket(
        path = "/ws/exceptions",
        isEnabledFlow = { isEnabledExceptions },
        initialData = { exceptionsDao.getAllSuspend() },
        dataFlow = { exceptionsDao.getAll() },
        getId = { it.id },
        debounceTimeMillis = 0, // No debounce for exceptions
    )


    delete("exceptions") {
        if (isEnabledExceptions.first()) {
            exceptionsDao.deleteAll()
            call.respond(HttpStatusCode.OK, "All exceptions deleted")
        } else {
            call.respond(HttpStatusCode.BadRequest, "Exception monitoring is disabled")
        }
    }
    webSocket("/ws/exceptions/{id}") {
        val id = call.parameters["id"]?.toLongOrNull()
        if (id != null) {
            if (isEnabledExceptions.first()) {
                sendSerialized(exceptionsDao.getByIDSync(id))
            } else{
                sendSerialized(null)
            }
            launch {
                combine(
                    exceptionsDao.getByID(id),
                    isEnabledExceptions
                ) { exception, isEnabled ->
                    isEnabled to exception
                }.collect { (isEnabled, exception) ->
                    if (isEnabled) {
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
}