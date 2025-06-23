package com.oriooneee.axer.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.oriooneee.axer.domain.requests.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
internal interface RequestDao {
    @Query("SELECT * FROM Transactions")
    fun getAll(): Flow<List<Transaction>>


    @Upsert
    suspend fun upsert(request: Transaction): Long

    @Delete
    suspend fun delete(request: Transaction)

    @Query("DELETE FROM Transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

//    suspend fun update(request: Request){
//    }

    @Query("SELECT * FROM Transactions ORDER BY id DESC LIMIT 5")
    suspend fun getFirstFive(): List<Transaction>


    @Query("DELETE FROM Transactions")
    suspend fun deleteAll()

    @Query("SELECT * FROM Transactions WHERE id = :id")
    fun getById(id: Long?): Flow<Transaction?>

    @Query("UPDATE Transactions SET isViewed = :isViewed WHERE id = :id")
    suspend fun updateViewed(id: Long, isViewed: Boolean)
}