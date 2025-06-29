package sample.app.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import sample.app.room.entity.Director

@Dao
interface DirectorDao {

    @Upsert
    suspend fun upsertDirector(director: Director)

    @Upsert
    suspend fun upsertDirectors(directors: List<Director>)

    @Query("SELECT * FROM Director")
    suspend fun getAllDirectors(): List<Director>

    @Query("DELETE FROM Director")
    suspend fun deleteAll()
}
