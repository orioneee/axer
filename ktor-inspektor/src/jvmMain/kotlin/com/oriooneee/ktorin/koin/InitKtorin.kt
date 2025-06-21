package com.oriooneee.ktorin.koin

import org.koin.dsl.koinApplication

object InitKtorin {
    fun init() {
        IsolatedContext.koinApp = koinApplication {
            modules(Modules.getModules())
        }
    }
}