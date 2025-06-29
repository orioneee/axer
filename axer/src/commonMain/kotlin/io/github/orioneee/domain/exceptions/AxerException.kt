package io.github.orioneee.domain.exceptions

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity()
internal data class AxerException(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val time: Long,
    val message: String,
    val shortName: String,
    val stackTrace: String,
    val isFatal: Boolean,
)