package io.github.orioneee.internal.logger

import io.github.aakira.napier.LogLevel

internal expect object PlatformLogger {
    fun performPlatformLog(
        priority: LogLevel,
        tag: String?,
        throwable: Throwable?,
        message: String?,
        time: Long,
    )
}