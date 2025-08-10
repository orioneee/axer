package io.github.orioneee.internal.koin

import io.github.orioneee.internal.room.getDatabaseBuilder
import io.github.orioneee.internal.room.getAxerDatabase
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