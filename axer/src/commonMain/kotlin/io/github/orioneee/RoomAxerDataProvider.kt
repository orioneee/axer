package io.github.orioneee

import io.github.orioneee.domain.database.DatabaseWrapped
import io.github.orioneee.domain.exceptions.AxerException
import io.github.orioneee.domain.logs.LogLine
import io.github.orioneee.domain.requests.Transaction
import io.github.orioneee.processors.RoomReader
import io.github.orioneee.room.AxerDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class RoomAxerDataProvider(
    database: AxerDatabase
) : AxerDataProvider {
    private val requestDao = database.getRequestDao()
    private val exceptionDao = database.getAxerExceptionDao()
    private val logDao = database.getLogsDao()

    private val reader = RoomReader()

    override fun getAllRequests(): Flow<List<Transaction>> = requestDao.getAll()

    override fun getRequestById(id: Long): Flow<Transaction?> = requestDao.getById(id)
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

    override fun getDatabases(): Flow<List<DatabaseWrapped>> = flow {
        val tables = reader.getTablesFromAllDatabase()
        emit(tables)
    }

}