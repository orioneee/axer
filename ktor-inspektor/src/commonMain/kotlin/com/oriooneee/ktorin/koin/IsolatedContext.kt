package com.oriooneee.ktorin.koin

import org.koin.core.KoinApplication

object IsolatedContext {
    lateinit var koinApp: KoinApplication
    val koin by lazy {
        koinApp.koin
    }
}