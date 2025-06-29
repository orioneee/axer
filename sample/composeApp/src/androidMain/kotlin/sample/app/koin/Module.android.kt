package sample.app.koin

import android.content.Context
import org.koin.core.module.Module
import org.koin.dsl.module
import sample.app.room.getDatabaseBuilder
import sample.app.room.getSampleDatabase

actual fun getPlatformModule(): Module {
    return module {
        single {
            val context: Context = get()
            val builder = getDatabaseBuilder(context)
            getSampleDatabase(builder)
        }
    }
}