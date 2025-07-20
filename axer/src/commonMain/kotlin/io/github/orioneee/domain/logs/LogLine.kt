package io.github.orioneee.domain.logs

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.aakira.napier.LogLevel
import io.github.orioneee.logger.formateAsDate
import kotlinx.serialization.Serializable

@Serializable
@Entity
data class LogLine(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val tag: String?,
    val message: String,
    val level: LogLevel,
    val time: Long,
) {
    override fun toString(): String {
        val infoString =
            "${time.formateAsDate()} - ${level.name} - ${tag} - "
        val spacesString =
            List(infoString.length) { " " }.joinToString("")
        val formatedSMessage =
            message.replace("\n", "\n$spacesString")
        return "$infoString$formatedSMessage"
    }
}