package io.github.orioneee

import io.github.orioneee.internal.koin.IsolatedContext
import io.github.orioneee.internal.koin.Modules
import org.koin.dsl.koinApplication

internal actual fun openAxer() {}
internal actual fun initializeIfCan() {
    IsolatedContext.initIfNotInited(
        koinApplication {
            modules(
                Modules.getModules()
            )
        }
    )
}