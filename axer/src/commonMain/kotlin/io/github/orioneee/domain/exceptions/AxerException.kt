package io.github.orioneee.domain.exceptions

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.orioneee.domain.requests.data.SavableError
import io.github.orioneee.domain.requests.data.SavableErrorFull
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class AxerException(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val time: Long,
    @Embedded
    val error: SavableErrorFull,
    val isFatal: Boolean,

    val sessionIdentifier: String
)