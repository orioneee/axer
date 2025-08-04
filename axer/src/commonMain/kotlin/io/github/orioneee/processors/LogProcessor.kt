package io.github.orioneee.processors

import io.github.aakira.napier.LogLevel
import io.github.orioneee.domain.logs.LogLine
import io.github.orioneee.koin.IsolatedContext
import io.github.orioneee.logger.getPlatformStackTrace
import io.github.orioneee.room.dao.LogsDAO
import io.github.orioneee.storage.AxerSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.ExperimentalTime

internal object LogProcessor {
    private val logsDAO by lazy { IsolatedContext.koin.get<LogsDAO>() }
    val logSaveMutex = Mutex()

    @OptIn(ExperimentalTime::class)
    internal fun onLog(
        tag: String?,
        message: String?,
        level: LogLevel,
        throwable: Throwable?,
        time: Long
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            if (AxerSettings.isRecordingLogs.get()) {
                logSaveMutex.withLock {
                    val newMessage = message +
                            (throwable?.let { "\n${it.getPlatformStackTrace()}" } ?: "")
                    val line = LogLine(
                        tag = tag,
                        message = newMessage.let {
                            if (it.length > 5000) it.take(5000) + "... (truncated)" else it
                        },
                        level = level,
                        time = time,
                        sessionIdentifier = SessionManager.sessionId
                    )
                    try {
                        logsDAO.deleteAllWhichOlderThan()
                        logsDAO.upsert(line)
                    } catch (e: Exception) {
                        println(e.getPlatformStackTrace())
                    }
                }
            }
        }
    }
}