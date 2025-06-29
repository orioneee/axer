package io.github.orioneee

import android.app.Application
import io.github.orioneee.koin.IsolatedContext
import io.github.orioneee.koin.Modules
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.koinApplication


object AxerManager {
    fun init(application: Application) {
        IsolatedContext.initIfNotInited(
            koinApplication {
                androidContext(application)
                modules(Modules.getModules())
            }
        )
    }
}