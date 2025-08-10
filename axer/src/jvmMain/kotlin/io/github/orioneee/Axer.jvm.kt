package io.github.orioneee

import io.github.orioneee.internal.koin.IsolatedContext
import io.github.orioneee.internal.koin.Modules
import org.koin.dsl.koinApplication

actual fun openAxer() {}
actual fun initializeIfCan() {
    IsolatedContext.initIfNotInited(
        koinApplication {
            modules(
                Modules.getModules()
            )
        }
    )
}