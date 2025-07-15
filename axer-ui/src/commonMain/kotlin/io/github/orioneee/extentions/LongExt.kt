package io.github.orioneee.extentions

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(FormatStringsInDatetimeFormats::class, ExperimentalTime::class)
internal fun Long.formateAsDate(): String {
    val instant = Instant.fromEpochMilliseconds(this)
    val timeZone = TimeZone.currentSystemDefault()
    val dateTime = instant.toLocalDateTime(timeZone)

    val format = LocalDateTime.Format {
        byUnicodePattern("dd.MM.yyyy HH:mm:ss:SSS")
    }

    return dateTime.format(format)
}