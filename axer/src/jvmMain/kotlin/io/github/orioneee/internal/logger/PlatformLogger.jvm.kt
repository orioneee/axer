package io.github.orioneee.internal.logger

import io.github.aakira.napier.LogLevel
import io.github.orioneee.internal.logger.formateAsDate
import io.github.orioneee.internal.logger.getPlatformStackTrace
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

actual object PlatformLogger {
    private val logMutex = Mutex()

    actual suspend fun log(
        priority: LogLevel,
        tag: String?,
        throwable: Throwable?,
        message: String?,
        time: Long,
    ) {
        logMutex.withLock {
            val timestamp = time.formateAsDate()
            val logLine = "$timestamp [${priority.name}] ${tag.orEmpty()} - ${message.orEmpty()}" +
                    if (throwable != null) "\n\t${throwable.getPlatformStackTrace()}" else ""

            if (priority == LogLevel.ERROR || priority == LogLevel.ASSERT) {
                System.err.println(logLine)
            } else {
                println(logLine)
            }
        }
    }
}