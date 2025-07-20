package io.github.orioneee.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import io.github.orioneee.domain.exceptions.AxerException
import kotlinx.coroutines.flow.Flow

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

    @Upsert
    suspend fun upsert(axerException: AxerException): Long

    @Query("SELECT * FROM AxerException")
    suspend fun getAllSuspend(): List<AxerException>
}