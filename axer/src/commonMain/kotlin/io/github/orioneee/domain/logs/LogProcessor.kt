package io.github.orioneee.domain.logs

import io.github.aakira.napier.LogLevel
import io.github.orioneee.extentions.getPlatformStackTrace
import io.github.orioneee.koin.IsolatedContext
import io.github.orioneee.room.dao.LogsDAO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class LogProcessor {
    private val logsDAO: LogsDAO by IsolatedContext.koin.inject()

    @OptIn(ExperimentalTime::class)
    internal fun onLog(
        tag: String?,
        message: String?,
        level: LogLevel,
        throwable: Throwable?,
        time: Long
    ) {
        val newMessage = message +
                (throwable?.let { "\n${it.getPlatformStackTrace()}" } ?: "")
        val line = LogLine(
            tag = tag,
            message = newMessage,
            level = level,
            time = time
        )
        CoroutineScope(Dispatchers.IO).launch {
            logsDAO.upsert(line)
        }
    }
}