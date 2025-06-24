package com.oriooneee.axer

import java.lang.Thread.UncaughtExceptionHandler

open class AxerUncaughtExceptionHandler : UncaughtExceptionHandler {
    private val defaultHandler: UncaughtExceptionHandler? =
        Thread.getDefaultUncaughtExceptionHandler()


    open override fun uncaughtException(p0: Thread, p1: Throwable) {
        if(defaultHandler != null) {
            defaultHandler.uncaughtException(p0, p1)
        } else {
            logAboutException(p1, exit = true)
        }
    }
}

expect fun logAboutException(exception: Throwable, exit: Boolean)