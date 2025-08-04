package io.github.orioneee.logger

import android.util.Log
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
}