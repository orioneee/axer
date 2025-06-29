package sample.app.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Director(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val firstName: String,
    val lastName: String
)