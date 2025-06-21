package sample.app

import android.app.Application
import com.oriooneee.Ktorin
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class SampleApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        Ktorin.init(this)
        startKoin {
            androidContext(this@SampleApplication)
            modules(SampleKoinModule.module)
        }
    }
}