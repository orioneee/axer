package io.github.orioneee.extentions

import kotlinx.datetime.Instant.Companion.fromEpochMilliseconds
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toStdlibInstant
import kotlin.time.ExperimentalTime

@OptIn(FormatStringsInDatetimeFormats::class, ExperimentalTime::class)
internal fun Long.formateAsTime(): String {
    val instant = fromEpochMilliseconds(this)
    val timeZone = TimeZone.currentSystemDefault()
    val dateTime = instant.toStdlibInstant().toLocalDateTime(timeZone)

    val format = LocalDateTime.Format {
        byUnicodePattern("HH:mm:ss")
    }

    return dateTime.format(format)

}