package com.oriooneee

import android.app.Application
import com.oriooneee.axer.koin.IsolatedContext
import com.oriooneee.axer.koin.Modules
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