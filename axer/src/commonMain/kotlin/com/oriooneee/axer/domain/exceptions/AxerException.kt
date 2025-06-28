package com.oriooneee.axer.domain.exceptions

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant.Companion.fromEpochMilliseconds
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlin.time.ExperimentalTime

@Entity()
data class AxerException(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val time: Long,
    val message: String,
    val shortName: String,
    val stackTrace: String,
    val isFatal: Boolean,
){
    @OptIn(ExperimentalTime::class, FormatStringsInDatetimeFormats::class)
    fun formatedTime(): String{
        val date = fromEpochMilliseconds(time)
        val format = DateTimeComponents.Format {
            byUnicodePattern("HH:mm:ss")
        }
        return date.format(format)
    }
}