package io.github.orioneee

import io.github.orioneee.koin.IsolatedContext
import io.github.orioneee.koin.Modules
import org.koin.dsl.koinApplication

@Deprecated("No longer needed initialization on jvm")
fun Axer.initialize() {
    IsolatedContext.initIfNotInited(
        koinApplication {
            modules(Modules.getModules())
        }
    )
}