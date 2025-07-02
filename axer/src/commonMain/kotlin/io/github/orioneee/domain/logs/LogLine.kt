package io.github.orioneee.domain.logs

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.aakira.napier.LogLevel

@Entity
internal data class LogLine(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val tag: String?,
    val message: String,
    val level: LogLevel,
    val time: Long,
)