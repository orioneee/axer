package io.github.orioneee

import io.github.orioneee.domain.database.DatabaseWrapped
import io.github.orioneee.domain.exceptions.AxerException
import io.github.orioneee.domain.logs.LogLine
import io.github.orioneee.domain.requests.Transaction
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
}