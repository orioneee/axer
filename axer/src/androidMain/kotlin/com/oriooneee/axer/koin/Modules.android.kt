package com.oriooneee.axer.koin

import com.oriooneee.axer.room.getDatabaseBuilder
import com.oriooneee.axer.room.getAxerDatabase
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun getPlatformModules(): Module {
    return module {
        single {
            val builder = getDatabaseBuilder(get())
            getAxerDatabase(builder)
        }
    }
}