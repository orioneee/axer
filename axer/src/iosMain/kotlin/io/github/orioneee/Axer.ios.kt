package io.github.orioneee

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import io.github.orioneee.internal.koin.IsolatedContext
import io.github.orioneee.internal.koin.Modules
import org.koin.dsl.koinApplication
import platform.UIKit.UIApplication
import kotlin.experimental.ExperimentalNativeApi

actual fun openAxer() {
    val topController =
        UIApplication.sharedApplication.keyWindow?.rootViewController
            ?: throw IllegalStateException("No root view controller found")

    topController.presentViewController(
        ComposeUIViewController {
            Scaffold {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = it.calculateTopPadding())
                ) {
                    AxerUIEntryPoint().Screen()
                }
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