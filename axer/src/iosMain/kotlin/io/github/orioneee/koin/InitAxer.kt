package io.github.orioneee.koin

import org.koin.dsl.koinApplication

object InitAxer {
    fun initialize() {
        IsolatedContext.initIfNotInited(koinApplication {
            modules(getPlatformModules(), Modules.daoModule, Modules.viewModelModule)
        }
        )
    }
}