package io.github.orioneee.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Upsert
import io.github.orioneee.domain.exceptions.AxerException
import io.github.orioneee.domain.exceptions.SessionEvent
import io.github.orioneee.domain.exceptions.SessionException
import io.github.orioneee.domain.logs.LogLine
import io.github.orioneee.domain.other.SessionEventDTO
import io.github.orioneee.domain.other.toSessionEvents
import io.github.orioneee.domain.requests.data.TransactionShort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

@Dao
internal interface AxerExceptionDao {
    @Query("SELECT * FROM AxerException ORDER BY time DESC")
    fun getAll(): Flow<List<AxerException>>

    @Query("DELETE FROM AxerException")
    suspend fun deleteAll()

    @Query("SELECT * FROM AxerException WHERE id = :id")
    fun getByID(id: Long?): Flow<AxerException?>


    @Query("SELECT * FROM AxerException WHERE id = :id")
    suspend fun getByIDSync(id: Long?): AxerException?


    @RewriteQueriesToDropUnusedColumns
    @Query(
        """
    SELECT 
    "transaction" AS eventType,
        id AS transaction_id, method AS transaction_method, sendTime AS transaction_sendTime,
        host AS transaction_host, path AS transaction_path, responseTime AS transaction_responseTime,
        responseStatus AS transaction_responseStatus, error_name AS transaction_error_name,
        error_message AS transaction_error_message, responseDefaultType AS transaction_responseDefaultType,
        isViewed AS transaction_isViewed, sessionIdentifier AS transaction_sessionIdentifier,
        
        NULL AS error_id, NULL AS error_time, NULL AS error_isFatal,
        NULL AS error_sessionIdentifier, NULL AS error_name, NULL AS error_message,
        NULL AS error_stackTrace, NULL AS error_cause_name, NULL AS error_cause_message,
        NULL AS error_cause_stackTrace,
        
        NULL AS log_id, NULL AS log_tag, NULL AS log_message, NULL AS log_level, NULL AS log_time, NULL AS log_sessionIdentifier,
        sendTime AS eventTime
    FROM Transactions WHERE sessionIdentifier = :sessionIdentifier AND sendTime <= :olderThan
    
    UNION ALL
    
    SELECT 
        "exception" AS eventType,
        NULL AS transaction_id, NULL AS transaction_method, NULL AS transaction_sendTime,
        NULL AS transaction_host, NULL AS transaction_path, NULL AS transaction_responseTime,
        NULL AS transaction_responseStatus, NULL AS transaction_error_name,
        NULL AS transaction_error_message, NULL AS transaction_responseDefaultType,
        NULL AS transaction_isViewed, NULL AS transaction_sessionIdentifier,
        
        id AS error_id, time AS error_time, isFatal AS error_isFatal,
        sessionIdentifier AS error_sessionIdentifier, 
        name AS error_name,
        message AS error_message,
        stackTrace AS error_stackTrace,
        NULL AS error_cause_name, 
        NULL AS error_cause_message,
        NULL AS error_cause_stackTrace,
        
        NULL AS log_id, NULL AS log_tag, NULL AS log_message, NULL AS log_level, NULL AS log_time, NULL AS log_sessionIdentifier,
        time AS eventTime
    FROM AxerException WHERE sessionIdentifier = :sessionIdentifier AND time <= :olderThan
    
    UNION ALL
    
    SELECT
    "log" AS eventType,
        NULL AS transaction_id, NULL AS transaction_method, NULL AS transaction_sendTime,
        NULL AS transaction_host, NULL AS transaction_path, NULL AS transaction_responseTime,
        NULL AS transaction_responseStatus, NULL AS transaction_error_name,
        NULL AS transaction_error_message, NULL AS transaction_responseDefaultType,
        NULL AS transaction_isViewed, NULL AS transaction_sessionIdentifier,
        
        NULL AS error_id, NULL AS error_time, NULL AS error_isFatal,
        NULL AS error_sessionIdentifier, NULL AS error_name, NULL AS error_message,
        NULL AS error_stackTrace, NULL AS error_cause_name, NULL AS error_cause_message,
        NULL AS error_cause_stackTrace,
        
        id AS log_id, tag AS log_tag, message AS log_message, level AS log_level, 
        time AS log_time, sessionIdentifier AS log_sessionIdentifier,
        time AS eventTime
    FROM LogLine WHERE sessionIdentifier = :sessionIdentifier AND time <= :olderThan
    
    ORDER BY eventTime DESC
    LIMIT 25
"""
    )
    suspend fun getSessionEventsDTO(
        sessionIdentifier: String,
        olderThan: Long,
    ): List<SessionEventDTO>


    suspend fun getSessionEvents(
        exceptionId: Long
    ): SessionException? {
        val exception = getByIDSync(exceptionId) ?: return null
        return SessionException(
            exception = exception,
            events = getSessionEventsDTO(
                exception.sessionIdentifier,
                exception.time
            ).toSessionEvents()
                .sortedByDescending {
                it.eventTime
            }.also {
                println("Session events for exception ${exception.id}: ${it.size}")
            }
        )
    }

    @Upsert
    suspend fun upsert(axerException: AxerException): Long

    @Query("SELECT * FROM AxerException")
    suspend fun getAllSuspend(): List<AxerException>
}