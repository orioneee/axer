package com.oriooneee

import android.app.Application
import com.oriooneee.ktorin.koin.Modules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

object Ktorin {
    fun init(application: Application){
        startKoin {
            androidContext(application)
            modules(Modules.getModules())
        }
    }
}