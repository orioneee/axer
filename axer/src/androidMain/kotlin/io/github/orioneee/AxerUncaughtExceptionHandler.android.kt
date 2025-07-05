package io.github.orioneee

import android.util.Log
import io.github.orioneee.logger.getPlatformStackTrace
import kotlin.system.exitProcess

internal actual fun logAboutException(exception: Throwable, exit: Boolean) {
    Log.e(
        "",
        exception.getPlatformStackTrace(),
    )
    android.os.Process.killProcess(android.os.Process.myPid())
    exitProcess(10)
}