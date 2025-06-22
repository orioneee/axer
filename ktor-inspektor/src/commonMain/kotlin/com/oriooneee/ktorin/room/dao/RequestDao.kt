package com.oriooneee.ktorin.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.oriooneee.ktorin.domain.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface RequestDao {
    @Query("SELECT * FROM Request")
    fun getAll(): Flow<List<Transaction>>


    @Upsert
    suspend fun upsert(request: Transaction): Long

    @Delete
    suspend fun delete(request: Transaction)

    @Query("DELETE FROM Request WHERE id = :id")
    suspend fun deleteById(id: Long)

//    suspend fun update(request: Request){
//    }

    @Query("SELECT * FROM Request ORDER BY id DESC LIMIT 5")
    suspend fun getFirstFive(): List<Transaction>


    @Query("DELETE FROM Request")
    suspend fun deleteAll()

    @Query("SELECT * FROM Request WHERE id = :id")
    fun getById(id: Long?): Flow<Transaction?>

    @Query("UPDATE Request SET isViewed = :isViewed WHERE id = :id")
    suspend fun updateViewed(id: Long, isViewed: Boolean)
}