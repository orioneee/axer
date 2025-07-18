package io.github.orioneee.koin

import io.github.orioneee.room.AxerDatabase
import io.github.orioneee.room.getDatabaseBuilder
import io.github.orioneee.room.getAxerDatabase
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun getPlatformModules(): Module {
    return module {
        single<AxerDatabase> {
            val builder = getDatabaseBuilder(get())
            getAxerDatabase(builder)
        }
    }
}