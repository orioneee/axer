package sample.app

import android.app.Application
import io.github.orioneee.AxerManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import sample.app.koin.KoinModules

class SampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AxerManager.init(this)
        startKoin {
            androidContext(this@SampleApplication)
            modules(KoinModules.module)
        }
    }
}