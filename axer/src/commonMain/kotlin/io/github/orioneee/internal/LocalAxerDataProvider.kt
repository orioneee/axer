package io.github.orioneee.internal

import io.github.orioneee.internal.domain.database.DatabaseData
import io.github.orioneee.internal.domain.database.DatabaseWrapped
import io.github.orioneee.internal.domain.database.EditableRowItem
import io.github.orioneee.internal.domain.database.QueryResponse
import io.github.orioneee.internal.domain.database.RowItem
import io.github.orioneee.internal.domain.exceptions.AxerException
import io.github.orioneee.internal.domain.exceptions.SessionException
import io.github.orioneee.internal.domain.logs.LogLine
import io.github.orioneee.internal.domain.other.DataState
import io.github.orioneee.internal.domain.other.EnabledFeathers
import io.github.orioneee.internal.domain.requests.data.TransactionFull
import io.github.orioneee.internal.domain.requests.data.TransactionShort
import io.github.orioneee.internal.processors.RoomReader
import io.github.orioneee.internal.room.AxerDatabase
import io.github.orioneee.internal.storage.AxerSettings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart

internal class LocalAxerDataProvider(
    database: AxerDatabase
) : AxerDataProvider {
    private val requestDao = database.getRequestDao()
    private val exceptionDao = database.getAxerExceptionDao()
    private val logDao = database.getLogsDao()

    private val reader = RoomReader()

    override fun getAllRequests(): Flow<DataState<List<TransactionShort>>> =
        requestDao.getAllShort().map {
            val state: DataState<List<TransactionShort>> = DataState.Success(it)
            state
        }.onStart {
            emit(DataState.Loading())
        }

    override suspend fun getDataForExportAsHar(): Result<List<TransactionFull>> =
        Result.success(requestDao.getAllSync())

    override fun getRequestById(id: Long): Flow<DataState<TransactionFull?>> =
        requestDao.getById(id).map {
            val state: DataState<TransactionFull?> = DataState.Success(it)
            state
        }.onStart {
            emit(DataState.Loading())
        }

    override suspend fun markAsViewed(id: Long): Result<Unit> {
        try {
            val request = requestDao.getByIdSync(id)
            if (request != null) {
                requestDao.updateViewed(id, true)
                return Result.success(Unit)
            } else {
                return Result.failure(IllegalArgumentException("Request with id $id not found"))
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    override suspend fun deleteAllRequests(): Result<String> {
        try {
            requestDao.deleteAll()
            return Result.success("All requests deleted successfully.")
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    override fun getAllExceptions(): Flow<DataState<List<AxerException>>> =
        exceptionDao.getAll().map {
            val state: DataState<List<AxerException>> = DataState.Success(it)
            state
        }.onStart {
            emit(DataState.Loading())
        }

    override suspend fun getSessionEventsByException(id: Long): Result<SessionException> {
        return try {
            val events = exceptionDao.getSessionEvents(id)
                ?: throw IllegalArgumentException("Exception with id $id not found")
            Result.success(events)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun deleteAllExceptions(): Result<Unit> {
        try {
            exceptionDao.deleteAll()
            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    override fun getAllLogs(): Flow<DataState<List<LogLine>>> =
        logDao.getAll().map {
            val state: DataState<List<LogLine>> = DataState.Success(it)
            state
        }.onStart {
            emit(DataState.Loading())
        }

    override suspend fun deleteAllLogs(): Result<Unit> {
        try {
            logDao.clear()
            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    override fun getDatabases(): Flow<DataState<List<DatabaseWrapped>>> {
        val initial = flow {
            val state: DataState<List<DatabaseWrapped>> =
                DataState.Success(reader.getTablesFromAllDatabase())
            emit(state)
        }

        val updates = reader.axerDriver.changeDataFlow
            .flatMapLatest {
                flow {
                    val state: DataState<List<DatabaseWrapped>> =
                        DataState.Success(reader.getTablesFromAllDatabase())
                    emit(state)
                }
            }

        return merge(initial, updates).onStart {
            emit(DataState.Loading())
        }
    }


    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    override fun getDatabaseContent(
        file: String,
        tableName: String,
        page: Int,
        pageSize: Int
    ): Flow<DataState<DatabaseData>> {
        val initial = flow {
            val content = reader.getTableContent(file, tableName, page, pageSize)
            val schema = reader.getTableSchema(file, tableName)
            val size = reader.getTableSize(file, tableName)
            emit(DataState.Success(DatabaseData(schema, content, size)))
        }

        val updates = reader.axerDriver.changeDataFlow
            .flatMapLatest {
                flow {
                    val content = reader.getTableContent(file, tableName, page, pageSize)
                    val schema = reader.getTableSchema(file, tableName)
                    val size = reader.getTableSize(file, tableName)
                    val state: DataState<DatabaseData> =
                        DataState.Success(DatabaseData(schema, content, size))
                    emit(state)
                }
            }

        return merge(initial, updates).onStart {
            emit(DataState.Loading())
        }
    }


    override suspend fun clearTable(file: String, tableName: String): Result<String> {
        try {
            reader.clearTable(file, tableName)
            return Result.success("Table $tableName in file $file cleared successfully.")
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    @OptIn(FlowPreview::class)
    override fun getAllQueries(): Flow<DataState<String>> {
        return reader.axerDriver.allQueryFlow
            .map { query ->
                DataState.Success(query)
            }
    }

    override suspend fun updateCell(
        file: String,
        tableName: String,
        editableItem: EditableRowItem
    ): Result<Unit> {
        try {
            reader.updateCell(
                file = file,
                tableName = tableName,
                editableItem = editableItem
            )
            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    override suspend fun deleteRow(
        file: String,
        tableName: String,
        row: RowItem
    ): Result<Unit> {
        try {
            reader.deleteRow(
                file = file,
                tableName = tableName,
                row = row
            )
            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    override suspend fun executeRawQuery(file: String, query: String): Result<Unit> {
        try {
            reader.executeRawQuery(file, query)
            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    override fun executeRawQueryAndGetUpdates(
        file: String,
        query: String
    ): Flow<QueryResponse> {
        val initial = flow {
            try {
                val result = reader.executeRawQuery(file, query)
                println("Initial query result: $result")
                emit(result)
            } catch (e: Exception) {
                emit(
                    QueryResponse(
                        schema = emptyList(),
                        rows = emptyList(),
                    )
                )
            }
        }

        val updates = reader.axerDriver.changeDataFlow
            .flatMapLatest {
                flow {
                    try {
                        val result = reader.executeRawQuery(file, query)
                        println("Update query result: $result")
                        emit(result)
                    } catch (e: Exception) {
                        emit(
                            QueryResponse(
                                schema = emptyList(),
                                rows = emptyList(),
                            )
                        )
                    }
                }
            }

        return merge(initial, updates)
    }

    override fun isConnected(): Flow<Boolean> = MutableStateFlow(true)
    override fun getEnabledFeatures(): Flow<DataState<EnabledFeathers>> {
        return combine(
            AxerSettings.enableRequestMonitor.asFlow(),
            AxerSettings.enableExceptionMonitor.asFlow(),
            AxerSettings.enableLogMonitor.asFlow(),
            AxerSettings.enableDatabaseMonitor.asFlow()
        ) { request, exception, log, database ->
            EnabledFeathers(
                isEnabledRequests = request,
                isEnabledExceptions = exception,
                isEnabledLogs = log,
                isEnabledDatabase = database,
                isReadOnly = false
            )
        }.map {
            DataState.Success(it)
        }
    }
}