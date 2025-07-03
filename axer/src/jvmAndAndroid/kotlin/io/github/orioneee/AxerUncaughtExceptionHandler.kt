package io.github.orioneee

import java.lang.Thread.UncaughtExceptionHandler

open class AxerUncaughtExceptionHandler : UncaughtExceptionHandler {
    private val defaultHandler: UncaughtExceptionHandler? = Thread.getDefaultUncaughtExceptionHandler()

    open override fun uncaughtException(p0: Thread, p1: Throwable) {
        Axer.recordAsFatal(p1)
        if (defaultHandler == null) {
            logAboutException(p1, exit = true)
        } else {
            defaultHandler.uncaughtException(p0, p1)
        }
    }

    init {
        Axer.initIfCan()
    }
}

internal expect fun logAboutException(exception: Throwable, exit: Boolean)