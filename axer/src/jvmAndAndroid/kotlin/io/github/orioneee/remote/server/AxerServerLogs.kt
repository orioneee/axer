package io.github.orioneee.remote.server

import io.github.orioneee.room.dao.LogsDAO
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

internal fun Route.logsModule(
    logsDao: LogsDAO,
    isEnabledLogs: Flow<Boolean>
) {
    webSocket("/ws/logs") {
        if (isEnabledLogs.first()) {
            sendSerialized(logsDao.getAllSync())
        }
        launch {
            combine(
                logsDao.getAll(),
                isEnabledLogs
            ) { logs, isEnabled ->
                isEnabled to logs
            }.collect { (isEnabled, logs) ->
                if (isEnabled) {
                    sendSerialized(logs)
                }
            }
        }
        for (frame in incoming) {
        }
    }

    delete("logs") {
        if (isEnabledLogs.first()) {
            logsDao.clear()
            call.respond(HttpStatusCode.OK, "All logs deleted")
        } else {
            call.respond(HttpStatusCode.BadRequest, "Log monitoring is disabled")
        }
    }
}