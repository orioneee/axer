package com.oriooneee.axer.koin

import com.oriooneee.axer.presentation.screens.RequestViewModel
import com.oriooneee.axer.presentation.screens.exceptions.ExceptionsViewModel
import com.oriooneee.axer.presentation.screens.sandbox.SandboxViewModel
import com.oriooneee.axer.room.AxerDatabase
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

internal object Modules {
    fun getModules() = listOf(
        getPlatformModules(),
        daoModule,
        viewModelModule,
    )

    val daoModule = module {
        single {
            val database: AxerDatabase = get()
            database.getRequestDao()
        }
        single {
            val database: AxerDatabase = get()
            database.getAxerExceptionDao()
        }
    }

    val viewModelModule = module {
        viewModel { (requestId: Long?) ->
            RequestViewModel(
                requestDao = get(),
                requestId = requestId
            )
        }
        viewModel { (exceptionID: Long?) ->
            ExceptionsViewModel(
                exceptionDao = get(),
                exceptionID = exceptionID
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