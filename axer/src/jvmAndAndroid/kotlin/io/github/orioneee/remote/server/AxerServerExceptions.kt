package io.github.orioneee.remote.server

import io.github.orioneee.room.dao.AxerExceptionDao
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

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
        // No debounce for exceptions
        sendsToReplaceAll = 0
    )


    delete("exceptions") {
        if (isEnabledExceptions.first()) {
            exceptionsDao.deleteAll()
            call.respond(HttpStatusCode.OK, "All exceptions deleted")
        } else {
            call.respond(HttpStatusCode.BadRequest, "Exception monitoring is disabled")
        }
    }
    get("/exceptions/{id}") {
        try {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id != null) {
                if (isEnabledExceptions.first()) {
                    val data = exceptionsDao.getSessionEvents(id)
                    call.respond(HttpStatusCode.OK, message = data ?: "No data found for id $id")
                } else {
                    call.respond(HttpStatusCode.BadRequest, "Exception monitoring is disabled")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            call.respond(HttpStatusCode.BadRequest, "Invalid id format")
        }
    }
}