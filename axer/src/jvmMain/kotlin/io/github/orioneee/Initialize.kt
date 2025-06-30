package io.github.orioneee

import io.github.orioneee.koin.IsolatedContext
import io.github.orioneee.koin.Modules
import org.koin.dsl.koinApplication

fun Axer.initialize() {
    IsolatedContext.initIfNotInited(
        koinApplication {
            modules(Modules.getModules())
        }
    )
}