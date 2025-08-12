package io.github.orioneee.internal.koin

import io.github.orioneee.internal.AxerDataProvider
import io.github.orioneee.internal.presentation.screens.database.TableDetailsViewModel
import io.github.orioneee.internal.presentation.screens.database.allQueries.AllQueriesViewModel
import io.github.orioneee.internal.presentation.screens.database.rawQuery.RawQueryViewModel
import io.github.orioneee.internal.presentation.screens.database.tableList.ListDatabaseViewModel
import io.github.orioneee.internal.presentation.screens.exceptions.list.ExceptionListViewModel
import io.github.orioneee.internal.presentation.screens.exceptions.details.ExceptionDetailsViewModel
import io.github.orioneee.internal.presentation.screens.logView.LogViewViewModel
import io.github.orioneee.internal.presentation.screens.requests.details.RequestDetailsViewModel
import io.github.orioneee.internal.presentation.screens.requests.list.RequestListViewModel
import io.github.orioneee.internal.room.AxerDatabase
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

internal object Modules {
    fun getModules() = listOf(
        getPlatformModules(),
        daoModule,
        viewModelModule,
    )

    private val daoModule = module {
        single {
            val database: AxerDatabase = get()
            database.getRequestDao()
        }
        single {
            val database: AxerDatabase = get()
            database.getAxerExceptionDao()
        }
        single {
            val database: AxerDatabase = get()
            database.getLogsDao()
        }
    }

    private val viewModelModule = module {
        viewModel { (provider: AxerDataProvider) ->
            RequestListViewModel(
                dataProvider = provider
            )
        }
        viewModel { (provider: AxerDataProvider, requestId: Long) ->
            RequestDetailsViewModel(
                dataProvider = provider,
                requestId = requestId
            )
        }
        viewModel { (provider: AxerDataProvider) ->
            ExceptionListViewModel(provider)
        }
        viewModel { (provider: AxerDataProvider, exceptionID: Long?) ->
            ExceptionDetailsViewModel(provider, exceptionID)
        }
        viewModel { (provider: AxerDataProvider, file: String, tableName: String) ->
            TableDetailsViewModel(provider, file, tableName)
        }
        viewModel { (provider: AxerDataProvider, file: String) ->
            RawQueryViewModel(provider, file)
        }
        viewModel { (provider: AxerDataProvider) ->
            AllQueriesViewModel(provider)
        }
        viewModel { (provider: AxerDataProvider) ->
            LogViewViewModel(provider)
        }
        viewModel { (provider: AxerDataProvider) ->
            ListDatabaseViewModel(
                dataProvider = provider
            )
        }
    }
}

internal expect fun getPlatformModules(): Module
