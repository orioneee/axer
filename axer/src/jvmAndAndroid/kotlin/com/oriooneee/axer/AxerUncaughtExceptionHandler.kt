package com.oriooneee.axer

import java.lang.Thread.UncaughtExceptionHandler
import kotlin.time.ExperimentalTime

open class AxerUncaughtExceptionHandler : UncaughtExceptionHandler {
    @OptIn(ExperimentalTime::class)
    open override fun uncaughtException(p0: Thread?, p1: Throwable?) {
        p1?.let {
            Axer.recordAsFatal(it)
        }
    }
}