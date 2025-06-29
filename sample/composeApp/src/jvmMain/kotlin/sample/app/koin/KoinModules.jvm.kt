package sample.app.koin

import org.koin.core.module.Module
import org.koin.dsl.module
import sample.app.room.getDatabaseBuilder
import sample.app.room.getSampleDatabase

actual fun getPlatformModule(): Module {
    return module {
        single {
            getSampleDatabase(
                getDatabaseBuilder()
            )
        }
    }
}