package sample.app

import android.app.Application
import io.github.orioneee.Axer
import io.github.orioneee.initialize
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import sample.app.koin.KoinModules

class SampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@SampleApplication)
            modules(KoinModules.module)
        }
    }
}