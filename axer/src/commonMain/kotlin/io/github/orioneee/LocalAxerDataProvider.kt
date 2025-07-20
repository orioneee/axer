package io.github.orioneee

import io.github.orioneee.domain.database.DatabaseData
import io.github.orioneee.domain.database.DatabaseWrapped
import io.github.orioneee.domain.database.EditableRowItem
import io.github.orioneee.domain.database.QueryResponse
import io.github.orioneee.domain.database.RowItem
import io.github.orioneee.domain.exceptions.AxerException
import io.github.orioneee.domain.logs.LogLine
import io.github.orioneee.domain.other.EnabledFeathers
import io.github.orioneee.domain.requests.data.Transaction
import io.github.orioneee.domain.requests.data.TransactionFull
import io.github.orioneee.processors.RoomReader
import io.github.orioneee.room.AxerDatabase
import io.github.orioneee.storage.AxerSettings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge

internal class LocalAxerDataProvider(
    database: AxerDatabase
) : AxerDataProvider {
    private val requestDao = database.getRequestDao()
    private val exceptionDao = database.getAxerExceptionDao()
    private val logDao = database.getLogsDao()

    private val reader = RoomReader()

    override fun getAllRequests(): Flow<List<Transaction>> = requestDao.getAllShort()
    override suspend fun getDataForExportAsHar(): List<TransactionFull> = requestDao.getAllSync()

    override fun getRequestById(id: Long): Flow<TransactionFull?> = requestDao.getById(id)
    override suspend fun markAsViewed(id: Long) {
        try {
            val request = requestDao.getByIdSync(id)
            if (request != null) {
                requestDao.updateViewed(id, true)
            }
        } catch (e: Exception) {

        }
    }

    override suspend fun deleteAllRequests() {
        try {
            requestDao.deleteAll()
        } catch (e: Exception) {
        }
    }

    override fun getAllExceptions(): Flow<List<AxerException>> = exceptionDao.getAll()

    override fun getExceptionById(id: Long): Flow<AxerException?> =
        exceptionDao.getByID(id)

    override suspend fun deleteAllExceptions() {
        try {
            exceptionDao.deleteAll()
        } catch (e: Exception) {
        }
    }

    override fun getAllLogs(): Flow<List<LogLine>> = logDao.getAll()
    override suspend fun deleteAllLogs() {
        try {
            logDao.clear()
        } catch (e: Exception) {
        }
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    override fun getDatabases(): Flow<List<DatabaseWrapped>> {
        val initial = flow {
            emit(reader.getTablesFromAllDatabase())
        }

        val updates = reader.axerDriver.changeDataFlow
            .debounce(100)
            .flatMapLatest {
                flow {
                    emit(reader.getTablesFromAllDatabase())
                }
            }

        return merge(initial, updates)
    }


    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    override fun getDatabaseContent(
        file: String,
        tableName: String,
        page: Int,
        pageSize: Int
    ): Flow<DatabaseData> {
        val initial = flow {
            val content = reader.getTableContent(file, tableName, page, pageSize)
            val schema = reader.getTableSchema(file, tableName)
            val size = reader.getTableSize(file, tableName)
            emit(DatabaseData(schema, content, size))
        }

        val updates = reader.axerDriver.changeDataFlow
            .debounce(100)
            .flatMapLatest {
                flow {
                    println("Fetching content for table: $tableName in file: $file new query $it")
                    val content = reader.getTableContent(file, tableName, page, pageSize)
                    val schema = reader.getTableSchema(file, tableName)
                    val size = reader.getTableSize(file, tableName)
                    emit(DatabaseData(schema, content, size))
                }
            }

        return merge(initial, updates)
    }


    override suspend fun clearTable(file: String, tableName: String) {
        try {
            reader.clearTable(file, tableName)
        } catch (e: Exception) {
            // Handle exception if needed
        }
    }

    @OptIn(FlowPreview::class)
    override fun getAllQueries(): Flow<String> {
        return reader.axerDriver.allQueryFlow
            .map { query ->
                println("Received query: $query")
                query
            }
    }

    override suspend fun updateCell(
        file: String,
        tableName: String,
        editableItem: EditableRowItem
    ) {
        reader.updateCell(
            file = file,
            tableName = tableName,
            editableItem = editableItem
        )
    }

    override suspend fun deleteRow(
        file: String,
        tableName: String,
        row: RowItem
    ) {
        reader.deleteRow(
            file = file,
            tableName = tableName,
            row = row
        )
    }

    override suspend fun executeRawQuery(file: String, query: String) {
        try {
            reader.executeRawQuery(file, query)
        } catch (e: Exception) {
            // Handle exception if needed
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
            .debounce(100)
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
    override fun getEnabledFeatures(): Flow<EnabledFeathers> {
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
                isEnabledDatabase = database
            )

        }
    }

}