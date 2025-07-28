package io.github.orioneee.remote.server

import io.github.orioneee.room.dao.LogsDAO
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

internal fun Route.logsModule(
    logsDao: LogsDAO,
    isEnabledLogs: Flow<Boolean>
) {
    reactiveUpdatesSocket(
        path = "/ws/logs",
        isEnabledFlow = { isEnabledLogs },
        initialData = { logsDao.getAllSync() },
        dataFlow = { logsDao.getAll() },
        getId = { it.id },
        chunkSize = 5000,
        sendsToReplaceAll = 10
    )


    delete("logs") {
        if (isEnabledLogs.first()) {
            logsDao.clear()
            call.respond(HttpStatusCode.OK, "All logs deleted")
        } else {
            call.respond(HttpStatusCode.BadRequest, "Log monitoring is disabled")
        }
    }
}