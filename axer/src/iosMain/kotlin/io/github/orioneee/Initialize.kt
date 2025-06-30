package io.github.orioneee

import io.github.orioneee.koin.IsolatedContext
import io.github.orioneee.koin.Modules
import io.github.orioneee.koin.getPlatformModules
import org.koin.dsl.koinApplication


@Deprecated("No longer needed initialization on ios")
fun Axer.initialize() {
    IsolatedContext.initIfNotInited(
        koinApplication {
            modules(
                getPlatformModules(),
                Modules.daoModule,
                Modules.viewModelModule
            )
        }
    )
}