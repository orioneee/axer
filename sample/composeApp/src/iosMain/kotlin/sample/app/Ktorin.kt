package sample.app

import com.oriooneee.ktorin.koin.IsolatedContext
import com.oriooneee.ktorin.koin.Modules
import com.oriooneee.ktorin.koin.getPlatformModules
import org.koin.dsl.koinApplication

object InitKtorin {
    fun initializeShareLibrary() {
        IsolatedContext.koinApp = koinApplication {
            modules(getPlatformModules(), Modules.daoModule, Modules.viewModelModule)
        }
    }
}