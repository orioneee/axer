package io.github.orioneee.internal.logger

import io.github.aakira.napier.LogLevel

internal actual object PlatformLogger {
    actual fun performPlatformLog(
        priority: LogLevel,
        tag: String?,
        throwable: Throwable?,
        message: String?,
        time: Long,
    ) {
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