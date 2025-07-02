package io.github.orioneee.processors

import io.github.aakira.napier.LogLevel
import io.github.orioneee.extentions.formateAsDate
import io.github.orioneee.extentions.getPlatformStackTrace

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