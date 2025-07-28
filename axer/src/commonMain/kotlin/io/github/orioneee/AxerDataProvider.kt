package io.github.orioneee

import io.github.orioneee.domain.database.DatabaseData
import io.github.orioneee.domain.database.DatabaseWrapped
import io.github.orioneee.domain.database.EditableRowItem
import io.github.orioneee.domain.database.QueryResponse
import io.github.orioneee.domain.database.RowItem
import io.github.orioneee.domain.exceptions.AxerException
import io.github.orioneee.domain.exceptions.SessionException
import io.github.orioneee.domain.logs.LogLine
import io.github.orioneee.domain.other.DataState
import io.github.orioneee.domain.other.EnabledFeathers
import io.github.orioneee.domain.requests.data.TransactionFull
import io.github.orioneee.domain.requests.data.TransactionShort
import io.github.orioneee.presentation.screens.database.TableDetailsViewModel
import kotlinx.coroutines.flow.Flow

@Suppress("Dokka")

interface AxerDataProvider {
    fun getAllRequests(): Flow<DataState<List<TransactionShort>>>
    suspend fun getDataForExportAsHar(): Result<List<TransactionFull>>
    fun getRequestById(id: Long): Flow<DataState<TransactionFull?>>
    suspend fun markAsViewed(id: Long): Result<Unit>
    suspend fun deleteAllRequests(): Result<Unit>

    fun getAllExceptions(): Flow<DataState<List<AxerException>>>
    suspend fun getSessionEventsByException(id: Long): Result<SessionException?>
    suspend fun deleteAllExceptions(): Result<Unit>

    fun getAllLogs(): Flow<DataState<List<LogLine>>>
    suspend fun deleteAllLogs(): Result<Unit>

    fun getDatabases(): Flow<DataState<List<DatabaseWrapped>>>
    fun getDatabaseContent(
        file: String,
        tableName: String,
        page: Int = 0,
        pageSize: Int = TableDetailsViewModel.PAGE_SIZE,
    ): Flow<DataState<DatabaseData>>

    suspend fun clearTable(
        file: String,
        tableName: String,
    )

    fun getAllQueries(): Flow<DataState<String>>

    suspend fun updateCell(file: String, tableName: String, editableItem: EditableRowItem): Result<Unit>
    suspend fun deleteRow(file: String, tableName: String, row: RowItem): Result<Unit>

    suspend fun executeRawQuery(
        file: String,
        query: String,
    ): Result<Unit>

    fun executeRawQueryAndGetUpdates(
        file: String,
        query: String,
    ): Flow<QueryResponse>

    fun isConnected(): Flow<Boolean>

    fun getEnabledFeatures(): Flow<DataState<EnabledFeathers>>

}