package io.github.orioneee.processors

import io.github.aakira.napier.LogLevel
import io.github.orioneee.extentions.formateAsDate
import io.github.orioneee.extentions.getPlatformStackTrace
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter

actual fun performPlatformLog(
    priority: LogLevel,
    tag: String?,
    throwable: Throwable?,
    message: String?,
    time: Long
) {
    val date = time.formateAsDate()
    val tagMap: HashMap<LogLevel, String> = hashMapOf(
        LogLevel.VERBOSE to "💜 VERBOSE",
        LogLevel.DEBUG to "💚 DEBUG",
        LogLevel.INFO to "💙 INFO",
        LogLevel.WARNING to "💛 WARN",
        LogLevel.ERROR to "❤️ ERROR",
        LogLevel.ASSERT to "💞 ASSERT"
    )

    val logLine = "$date [${tagMap[priority]}] ${tag.orEmpty()} - ${message.orEmpty()}" +
            if (throwable != null) {
                "\n\t${throwable.getPlatformStackTrace()}"
            } else {
                ""
            }
    println(logLine)
}