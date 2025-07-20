package io.github.orioneee.koin

import io.github.orioneee.AxerDataProvider
import io.github.orioneee.presentation.screens.database.TableDetailsViewModel
import io.github.orioneee.presentation.screens.database.allQueries.AllQueriesViewModel
import io.github.orioneee.presentation.screens.database.rawQuery.RawQueryViewModel
import io.github.orioneee.presentation.screens.database.tableList.ListDatabaseViewModel
import io.github.orioneee.presentation.screens.exceptions.ExceptionsViewModel
import io.github.orioneee.presentation.screens.logView.LogViewViewModel
import io.github.orioneee.presentation.screens.requests.details.RequestDetailsViewModel
import io.github.orioneee.presentation.screens.requests.list.RequestListViewModel
import io.github.orioneee.room.AxerDatabase
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
        viewModel { (provider: AxerDataProvider, exceptionID: Long?) ->
            ExceptionsViewModel(
                provider,
                exceptionID = exceptionID
            )
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

expect fun getPlatformModules(): Module
