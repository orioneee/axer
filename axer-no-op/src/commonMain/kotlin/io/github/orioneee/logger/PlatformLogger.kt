package io.github.orioneee.logger

import io.github.aakira.napier.LogLevel

expect object PlatformLogger {
    fun performPlatformLog(
        priority: LogLevel,
        tag: String?,
        throwable: Throwable?,
        message: String?,
        time: Long,
    )
}