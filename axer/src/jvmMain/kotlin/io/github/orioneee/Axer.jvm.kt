package io.github.orioneee

import io.github.orioneee.koin.IsolatedContext
import io.github.orioneee.koin.Modules
import io.github.orioneee.koin.getPlatformModules
import org.koin.dsl.koinApplication

actual fun openAxer() {}
actual fun initializeIfCan() {
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