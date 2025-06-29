package io.github.orioneee.koin

import org.koin.dsl.koinApplication

object InitAxer {
    fun init() {
        IsolatedContext.initIfNotInited(
            koinApplication {
                modules(Modules.getModules())
            }
        )
    }
}