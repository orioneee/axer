package io.github.orioneee

import io.github.orioneee.koin.IsolatedContext
import io.github.orioneee.koin.Modules
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