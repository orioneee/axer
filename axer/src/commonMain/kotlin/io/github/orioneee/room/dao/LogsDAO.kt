package io.github.orioneee.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import io.github.aakira.napier.LogLevel
import io.github.orioneee.domain.logs.LogLine
import kotlinx.coroutines.flow.Flow

@Dao
internal interface LogsDAO {

    @Query("SELECT * FROM LogLine")
    fun getAll(): Flow<List<LogLine>>

    @Query("SELECT DISTINCT tag FROM LogLine")
    fun getUniqueTags(): Flow<List<String>>

    @Query("SELECT DISTINCT level FROM LogLine")
    fun getUniqueLevels(): Flow<List<LogLevel>>

    @Upsert
    suspend fun upsert(line: LogLine)

    @Query("DELETE FROM LogLine")
    suspend fun clear()
}