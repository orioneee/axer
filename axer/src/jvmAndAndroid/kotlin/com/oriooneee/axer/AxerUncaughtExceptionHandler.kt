package com.oriooneee.axer

import kotlinx.coroutines.runBlocking
import java.lang.Thread.UncaughtExceptionHandler

open class AxerUncaughtExceptionHandler : UncaughtExceptionHandler {
    private val defaultHandler: UncaughtExceptionHandler? =
        Thread.getDefaultUncaughtExceptionHandler()


    open override fun uncaughtException(p0: Thread, p1: Throwable) {
        runBlocking {
            Axer.recordAsFatal(p1).join()
        }
        defaultHandler?.uncaughtException(p0, p1)
    }
}

expect fun logAboutException(exception: Throwable, exit: Boolean)