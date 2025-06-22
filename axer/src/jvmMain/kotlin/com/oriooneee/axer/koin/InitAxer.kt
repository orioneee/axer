package com.oriooneee.axer.koin

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