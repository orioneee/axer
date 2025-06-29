package io.github.orioneee.koin

import io.github.orioneee.room.getDatabaseBuilder
import io.github.orioneee.room.getAxerDatabase
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun getPlatformModules(): Module {
    return module{
        single {
            val builder = getDatabaseBuilder()
            getAxerDatabase(builder)
        }
    }
}