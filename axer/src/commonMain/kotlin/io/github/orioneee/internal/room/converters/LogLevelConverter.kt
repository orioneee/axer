package io.github.orioneee.internal.room.converters

import androidx.room.TypeConverter
import io.github.aakira.napier.LogLevel

internal class LogLevelConverter {
    @TypeConverter
    fun fromLogLevel(value: String): LogLevel {
        return LogLevel.entries.find { it.name == value }
            ?: throw IllegalArgumentException("Unknown LogLevel: $value")
    }

    @TypeConverter
    fun logLevelToString(level: LogLevel): String {
        return level.name
    }
}