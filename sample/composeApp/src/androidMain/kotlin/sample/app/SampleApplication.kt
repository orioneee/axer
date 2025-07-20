package sample.app

import android.app.Application
import io.github.orioneee.Axer
import io.github.orioneee.remote.server.runServerIfNotRunning
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
        CoroutineScope(Dispatchers.Default).launch {
            Axer.runServerIfNotRunning(this)
        }
    }
}