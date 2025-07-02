package io.github.orioneee.processors

import io.github.aakira.napier.Antilog
import io.github.aakira.napier.LogLevel
import io.github.orioneee.domain.logs.LogProcessor
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class CleanAxerAntiLog() : Antilog() {
    @OptIn(ExperimentalTime::class)
    override fun performLog(
        priority: LogLevel,
        tag: String?,
        throwable: Throwable?,
        message: String?
    ) {
        val time = Clock.System.now().toEpochMilliseconds()
        performPlatformLog(priority, tag, throwable, message, time)
    }
}

class AxerLogSaver() : Antilog() {
    fun saveLog(
        priority: LogLevel,
        tag: String?,
        throwable: Throwable?,
        message: String?,
        time: Long,
    ) {
        val processor = LogProcessor()
        processor.onLog(
            level = priority,
            tag = tag,
            message = message,
            throwable = throwable,
            time = time
        )
    }

    @OptIn(ExperimentalTime::class)
    override fun performLog(
        priority: LogLevel,
        tag: String?,
        throwable: Throwable?,
        message: String?
    ) {
        val time = Clock.System.now().toEpochMilliseconds()
        saveLog(priority, tag, throwable, message, time)
        performPlatformLog(priority, tag, throwable, message, time)
    }
}

expect fun performPlatformLog(
    priority: LogLevel,
    tag: String?,
    throwable: Throwable?,
    message: String?,
    time: Long,
)