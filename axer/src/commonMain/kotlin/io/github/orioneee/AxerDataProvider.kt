package io.github.orioneee

import io.github.orioneee.domain.database.DatabaseData
import io.github.orioneee.domain.database.DatabaseWrapped
import io.github.orioneee.domain.database.EditableRowItem
import io.github.orioneee.domain.database.QueryResponse
import io.github.orioneee.domain.database.RowItem
import io.github.orioneee.domain.exceptions.AxerException
import io.github.orioneee.domain.logs.LogLine
import io.github.orioneee.domain.requests.Transaction
import io.github.orioneee.presentation.screens.database.TableDetailsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

interface AxerDataProvider {
    fun getAllRequests(): Flow<List<Transaction>>
    fun getRequestById(id: Long): Flow<Transaction?>
    suspend fun markAsViewed(id: Long)
    suspend fun deleteAllRequests()

    fun getAllExceptions(): Flow<List<AxerException>>
    fun getExceptionById(id: Long): Flow<AxerException?>
    suspend fun deleteAllExceptions()

    fun getAllLogs(): Flow<List<LogLine>>
    suspend fun deleteAllLogs()

    fun getDatabases(): Flow<List<DatabaseWrapped>>
    fun getDatabaseContent(
        file: String,
        tableName: String,
        page: Int = 0,
        pageSize: Int = TableDetailsViewModel.PAGE_SIZE,
    ): Flow<DatabaseData>

    suspend fun clearTable(
        file: String,
        tableName: String,
    )

    fun getAllQueries(): Flow<String>

    suspend fun updateCell(file: String, tableName: String, editableItem: EditableRowItem)
    suspend fun deleteRow(file: String, tableName: String, row: RowItem)

    suspend fun executeRawQuery(
        file: String,
        query: String,
    )

    fun excecuteRawQueryAndGetUpdates(
        file: String,
        query: String,
    ): Flow<QueryResponse>
}