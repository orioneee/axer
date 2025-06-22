package com.oriooneee.ktorin.koin

import com.oriooneee.ktorin.room.getDatabaseBuilder
import com.oriooneee.ktorin.room.getKtorinDatabase
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun getPlatformModules(): Module {
    return module {
        single {
            val builder = getDatabaseBuilder()
            getKtorinDatabase(builder)
        }
    }
}