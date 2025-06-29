package io.github.orioneee

import java.lang.Thread.UncaughtExceptionHandler

open class AxerUncaughtExceptionHandler : UncaughtExceptionHandler {
    private val defaultHandler: UncaughtExceptionHandler? =
        Thread.getDefaultUncaughtExceptionHandler()


    open override fun uncaughtException(p0: Thread, p1: Throwable) {
        defaultHandler?.uncaughtException(p0, p1)
    }
}