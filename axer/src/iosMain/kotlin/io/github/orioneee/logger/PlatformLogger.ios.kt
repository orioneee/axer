package io.github.orioneee.logger

import io.github.aakira.napier.LogLevel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

actual object PlatformLogger {
    private val logMutex = Mutex()

    actual suspend fun log(
        priority: LogLevel,
        tag: String?,
        throwable: Throwable?,
        message: String?,
        time: Long
    ) {
        logMutex.withLock {
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
}