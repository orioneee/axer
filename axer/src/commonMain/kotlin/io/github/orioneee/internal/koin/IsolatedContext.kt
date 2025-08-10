package io.github.orioneee.internal.koin

import org.koin.core.KoinApplication

internal object IsolatedContext {
    lateinit var koinApp: KoinApplication
        private set
    val koin by lazy {
        koinApp.koin
    }

    fun initIfNotInited(
        application: KoinApplication
    ) {
        if (!::koinApp.isInitialized) {
            koinApp = application
        }
    }
}