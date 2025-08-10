package io.github.orioneee

import android.content.Context
import io.github.orioneee.internal.koin.IsolatedContext
import io.github.orioneee.internal.koin.Modules
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.koinApplication


@Deprecated("No need to call this function anymore, Axer is initialized automatically")
fun Axer.initialize(applicationContext: Context) {
    val koinApplication = koinApplication {
        androidContext(applicationContext.applicationContext)
        modules(Modules.getModules())
    }
    IsolatedContext.initIfNotInited(koinApplication)
}