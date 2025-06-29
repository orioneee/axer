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
    indices = [Index(value = ["directorId"])] // This fixes the warning about indexing
)
data class Movie(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val releaseYear: Int,
    val rating: Float,
    val genre: String,
    val directorId: Long // reference to the Director's primary key
)
