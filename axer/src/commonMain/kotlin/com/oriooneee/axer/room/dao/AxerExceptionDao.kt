package com.oriooneee.axer.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.oriooneee.axer.domain.exceptions.AxerException
import kotlinx.coroutines.flow.Flow

@Dao
internal interface AxerExceptionDao {
    @Query("SELECT * FROM AxerException")
    fun getAll(): Flow<List<AxerException>>

    @Query("DELETE FROM AxerException")
    suspend fun deleteAll()

    @Query("SELECT * FROM AxerException WHERE id = :id")
    fun getByID(id: Long?): Flow<AxerException?>

    @Upsert
    suspend fun upsert(axerException: AxerException): Long

    @Query("SELECT * FROM AxerException")
    suspend fun getAllSuspend(): List<AxerException>
}