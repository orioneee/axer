package sample.app

import android.app.Application
import com.oriooneee.Ktorin

class SampleApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        Ktorin.init(this)
    }
}