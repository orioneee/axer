package io.github.orioneee.processors

import io.github.aakira.napier.Antilog
import io.github.aakira.napier.LogLevel
import io.github.orioneee.domain.logs.LogProcessor
import io.github.orioneee.logger.PlatformLogger
import io.github.orioneee.storage.AxerSettings
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

open class AxerLogSaver() : Antilog() {
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
        PlatformLogger.performPlatformLog(
            priority = priority,
            tag = tag,
            throwable = throwable,
            message = message,
            time = time
        )
        saveLog(priority, tag, throwable, message, time)
    }
}