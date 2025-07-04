package io.github.orioneee.processors

import io.github.aakira.napier.Antilog
import io.github.aakira.napier.LogLevel
import io.github.orioneee.logger.PlatformLogger
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

open class AxerLogSaver() : Antilog() {
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
    }
}