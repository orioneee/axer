package com.oriooneee

import android.app.Application
import com.oriooneee.ktorin.koin.IsolatedContext
import com.oriooneee.ktorin.koin.Modules
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.koinApplication


object Ktorin {
    fun init(application: Application) {
        IsolatedContext.koinApp = koinApplication {
            androidContext(application)
            modules(Modules.getModules())
        }
    }
}