package com.oriooneee.axer

import java.lang.Thread.UncaughtExceptionHandler
import kotlin.time.ExperimentalTime

open class AxerUncaughtExceptionHandler : UncaughtExceptionHandler {
    open override fun uncaughtException(p0: Thread?, p1: Throwable?) {
        p1?.let {
            it.printStackTrace()
            Axer.recordAsFatal(it)
        }
    }
}