package io.github.orioneee

import io.github.orioneee.internal.koin.IsolatedContext
import io.github.orioneee.internal.koin.Modules
import org.koin.dsl.koinApplication


@Deprecated("No longer needed initialization on ios")
fun Axer.initialize() {
    IsolatedContext.initIfNotInited(
        koinApplication {
            modules(
                Modules.getModules()
            )
        }
    )
}