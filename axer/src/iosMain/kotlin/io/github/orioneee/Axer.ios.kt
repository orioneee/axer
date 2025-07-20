package io.github.orioneee

import androidx.compose.ui.window.ComposeUIViewController
import io.github.orioneee.koin.IsolatedContext
import io.github.orioneee.koin.Modules
import io.github.orioneee.koin.getPlatformModules
import io.github.orioneee.presentation.AxerUIEntryPoint
import io.github.orioneee.presentation.LocalAxerDataProvider
import io.github.orioneee.processors.ExceptionProcessor
import io.github.orioneee.room.AxerDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.staticCFunction
import org.koin.compose.KoinIsolatedContext
import org.koin.dsl.koinApplication
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
            KoinIsolatedContext(IsolatedContext.koinApp) {
                val database: AxerDatabase = IsolatedContext.koinApp.koin.get()
                val provider = LocalAxerDataProvider(database)
                AxerUIEntryPoint().Screen(provider)
            }
        },
        animated = true,
        completion = null
    )
}

actual fun initializeIfCan() {
    IsolatedContext.initIfNotInited(
        koinApplication {
            modules(
                Modules.getModules()
            )
        }
    )
}

@OptIn(ExperimentalNativeApi::class)
fun Axer.installErrorHandler() {
    val current = getUnhandledExceptionHook()
    setUnhandledExceptionHook { exception ->
        recordAsFatal(
            exception,
            onRecorded = {
                if (current != null) {
                    current.invoke(exception)
                } else {
                    terminateWithUnhandledException(exception)
                }
            }
        )
    }
}

actual fun installErrorHandler() {
    Axer.installErrorHandler()
}