package sample.app.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Director::class,
            parentColumns = ["id"],
            childColumns = ["directorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["directorId"])]
)
data class Movie(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,
    val description: String,
    val releaseYear: Int,
    val rating: Float,
    val genre: String,
    val directorId: Long,

    val durationMinutes: Int,
    val budget: Long,
    val boxOffice: Long,
    val language: String,
    val country: String,
    val imdbId: String?,
    val posterUrl: String?,
    val trailerUrl: String?,
    val ageRating: String,
    val isAwardWinner: Boolean,
    val awardsCount: Int,
    val nominationsCount: Int,
    val filmingLocations: String?,
    val soundtrackComposer: String?,
    val productionCompany: String?
)

