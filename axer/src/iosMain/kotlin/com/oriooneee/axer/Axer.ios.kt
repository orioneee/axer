package com.oriooneee.axer

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.staticCFunction
import platform.Foundation.*
import platform.posix.*
import kotlin.experimental.ExperimentalNativeApi

actual fun openAxer() {
    //TODO
}


@OptIn(ExperimentalNativeApi::class)
@CName("customUncaughtExceptionHandler")
fun customUncaughtExceptionHandler(exception: NSException?) {
    val reason = exception?.reason ?: "Unknown reason"
    val stack = exception?.callStackSymbols?.joinToString("\n") ?: "No stack trace"
    val report = "Uncaught exception: $reason\nStack trace:\n$stack"

    Axer.recordAsFatal(
        Throwable(report),
        simpleName = exception?.name?.toString() ?: "UnknownException"
    )
    exit(1)
}

@OptIn(ExperimentalForeignApi::class)
actual fun installErrorHandler() {
    NSSetUncaughtExceptionHandler(staticCFunction(::customUncaughtExceptionHandler))
}
