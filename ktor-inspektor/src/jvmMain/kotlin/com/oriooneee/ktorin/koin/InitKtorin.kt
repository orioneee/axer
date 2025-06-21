package com.oriooneee.ktorin.koin

import org.koin.core.context.startKoin

object InitKtorin {
    fun init() {
        startKoin { 
            modules(Modules.getModules())
        }
    }
}