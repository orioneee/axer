package io.github.orioneee

import android.util.Log
import kotlin.system.exitProcess

internal actual fun logAboutException(exception: Throwable, exit: Boolean) {
    Log.e(
        "",
        exception.stackTraceToString(),
    )
    android.os.Process.killProcess(android.os.Process.myPid())
    exitProcess(10)
}