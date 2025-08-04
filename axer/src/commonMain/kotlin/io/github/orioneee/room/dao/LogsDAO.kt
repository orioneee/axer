package io.github.orioneee.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import io.github.aakira.napier.LogLevel
import io.github.orioneee.domain.logs.LogLine
import kotlinx.coroutines.flow.Flow
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Dao
internal interface LogsDAO {

    @Query("SELECT * FROM LogLine ORDER BY id DESC")
    fun getAll(): Flow<List<LogLine>>

    @Query("SELECT * FROM LogLine ORDER BY id DESC")
    suspend fun getAllSync(): List<LogLine>

    @Query("SELECT MAX(id) FROM LogLine")
    suspend fun getMaxId(): Long?

    @Query("SELECT DISTINCT tag FROM LogLine")
    fun getUniqueTags(): Flow<List<String>>

    @Query("SELECT DISTINCT level FROM LogLine")
    fun getUniqueLevels(): Flow<List<LogLevel>>

    @Upsert
    suspend fun upsert(line: LogLine): Long

    @Query("DELETE FROM LogLine")
    suspend fun clear()

    @Query("DELETE FROM LogLine WHERE time < :thresholdTime")
    suspend fun deleteOlderThan(thresholdTime: Long)

    @OptIn(ExperimentalTime::class)
    suspend fun deleteAllWhichOlderThan(timestampInSeconds: Long = 60 * 60 * 6) {
        val miliseconds = timestampInSeconds * 1000
        val currentTime = Clock.System.now().toEpochMilliseconds()
        val thresholdTime = currentTime - miliseconds
        deleteOlderThan(thresholdTime)
    }
}