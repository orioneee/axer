package com.oriooneee.axer

import kotlinx.datetime.Instant.Companion.fromEpochMilliseconds
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.toLocalDateTime

@OptIn(FormatStringsInDatetimeFormats::class)
fun Long.formateAsTime(): String {
    val instant = fromEpochMilliseconds(this)
    val timeZone = TimeZone.currentSystemDefault()
    val dateTime = instant.toLocalDateTime(timeZone)

    val format = LocalDateTime.Format {
        byUnicodePattern("HH:mm:ss")
    }

    return dateTime.format(format)

}