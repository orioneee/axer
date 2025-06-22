package com.oriooneee.ktorin.koin

import com.oriooneee.ktorin.presentation.screens.RequestViewModel
import com.oriooneee.ktorin.presentation.screens.sandbox.SandboxViewModel
import com.oriooneee.ktorin.room.KtorinDatabase
import com.oriooneee.ktorin.room.dao.RequestDao
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

object Modules {
    fun getModules() = listOf(
        getPlatformModules(),
        daoModule,
        viewModelModule,
    )

    val daoModule = module {
        single {
            val database: KtorinDatabase = get()
            database.getRequestDao()
        }
    }

    val viewModelModule = module {
        viewModel { (requestId: Long?) ->
            RequestViewModel(
                requestDao = get(),
                requestId = requestId
            )
        }
        viewModel { (requestId: Long?) ->
            SandboxViewModel(
                requestDao = get(),
                requestId = requestId
            )
        }
    }
}

expect fun getPlatformModules(): Module