package io.github.orioneee.internal.logger

import io.github.aakira.napier.LogLevel
import io.github.orioneee.internal.logger.formateAsDate
import io.github.orioneee.internal.logger.getPlatformStackTrace

internal actual object PlatformLogger {
    actual fun performPlatformLog(
        priority: LogLevel,
        tag: String?,
        throwable: Throwable?,
        message: String?,
        time: Long
    ) {
        val date = time.formateAsDate()
        val tagMap: HashMap<LogLevel, String> = hashMapOf(
            LogLevel.VERBOSE to "🟪 VERBOSE",
            LogLevel.DEBUG to "🟩 DEBUG",
            LogLevel.INFO to "🟦 INFO",
            LogLevel.WARNING to "🟨 WARN",
            LogLevel.ERROR to "🟥 ERROR",
            LogLevel.ASSERT to "🟫 ASSERT"
        )


        val logLine = "$date [${tagMap[priority]}] ${tag.orEmpty()} - ${message.orEmpty()}" +
                if (throwable != null) {
                    "\n\t${throwable.getPlatformStackTrace()}"
                } else {
                    ""
                }
        println(logLine)
    }
}