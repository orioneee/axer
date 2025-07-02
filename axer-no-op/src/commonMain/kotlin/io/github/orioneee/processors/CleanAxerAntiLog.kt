package io.github.orioneee.processors

import io.github.aakira.napier.Antilog
import io.github.aakira.napier.LogLevel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class CleanAxerAntiLog() : Antilog() {
    override fun performLog(
        priority: LogLevel,
        tag: String?,
        throwable: Throwable?,
        message: String?
    ) {
        performPlatformLog(priority, tag, throwable, message)
    }
}

class AxerLogSaver() : Antilog() {
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

expect fun performPlatformLog(
    priority: LogLevel,
    tag: String?,
    throwable: Throwable?,
    message: String?,
    time: Long
)