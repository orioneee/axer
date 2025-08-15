package io.github.orioneee.internal.logger

import android.util.Log
import io.github.aakira.napier.LogLevel

actual object PlatformLogger {
    actual fun performPlatformLog(
        priority: LogLevel,
        tag: String?,
        throwable: Throwable?,
        message: String?,
        time: Long
    ) {
        when (priority) {
            LogLevel.VERBOSE -> Log.v(tag, message, throwable)
            LogLevel.DEBUG -> Log.d(tag, message, throwable)
            LogLevel.INFO -> Log.i(tag, message, throwable)
            LogLevel.WARNING -> Log.w(tag, message, throwable)
            LogLevel.ERROR -> Log.e(tag, message, throwable)
            LogLevel.ASSERT -> Log.wtf(tag, message, throwable)
        }
    }
}