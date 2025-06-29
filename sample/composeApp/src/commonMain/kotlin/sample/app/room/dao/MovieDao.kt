package sample.app.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import sample.app.room.entity.Movie


@Dao
interface MovieDao {
    @Upsert
    suspend fun upsertMovie(movie: Movie)

    @Upsert
    suspend fun upsertMovies(movies: List<Movie>)

    @Query("SELECT * FROM Movie")
    suspend fun getAllMovies(): List<Movie>

    @Query("DELETE FROM Movie")
    suspend fun deleteAll()
}