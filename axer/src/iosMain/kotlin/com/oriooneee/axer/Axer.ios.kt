package com.oriooneee.axer

import androidx.compose.ui.window.ComposeUIViewController
import com.oriooneee.axer.presentation.AxerUIEntryPoint
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.staticCFunction
import platform.Foundation.NSException
import platform.Foundation.NSSetUncaughtExceptionHandler
import platform.UIKit.UIApplication
import platform.posix.exit
import kotlin.experimental.ExperimentalNativeApi

actual fun openAxer() {
    val topController =
        UIApplication.sharedApplication.keyWindow?.rootViewController
            ?: throw IllegalStateException("No root view controller found")

    topController.presentViewController(
        ComposeUIViewController {
            AxerUIEntryPoint.Screen(
                onClose = {
                    topController.dismissViewControllerAnimated(
                        flag = true,
                        completion = null
                    )
                }
            )
        },
        animated = true,
        completion = null
    )
}


@OptIn(ExperimentalNativeApi::class)
@CName("customUncaughtExceptionHandler")
fun customUncaughtExceptionHandler(exception: NSException?) {
    println("Custom uncaught exception handler called with exception: $exception")
    val reason = exception?.reason ?: "Unknown reason"
    println("Uncaught exception: $reason")
    val stack = "No stack trace"
    val report = "Uncaught exception: $reason\nStack trace:\n$stack"
    println("Uncaught exception: $report recording as fatal")

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
