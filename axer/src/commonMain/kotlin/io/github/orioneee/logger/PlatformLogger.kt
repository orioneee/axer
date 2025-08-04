package io.github.orioneee.logger

import io.github.aakira.napier.LogLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

expect object PlatformLogger {
    suspend fun log(
        priority: LogLevel,
        tag: String?,
        throwable: Throwable?,
        message: String?,
        time: Long,
    )
}
fun PlatformLogger.performPlatformLog(
    priority: LogLevel,
    tag: String?,
    throwable: Throwable?,
    message: String?,
    time: Long,
) {
    CoroutineScope(Dispatchers.IO).launch {
        PlatformLogger.log(priority, tag, throwable, message, time)
    }
}