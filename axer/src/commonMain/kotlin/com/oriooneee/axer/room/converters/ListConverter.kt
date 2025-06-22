package com.oriooneee.axer.room.converters

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json

internal class ListConverter {
    @TypeConverter
    fun fromListToString(list: List<String>?): String? {
        return Json.encodeToString(list ?: emptyList())
    }

    @TypeConverter
    fun fromStringToList(string: String?): List<String>? {
        return if (string.isNullOrEmpty()) {
            null
        } else {
            Json.decodeFromString<List<String>>(string)
        }
    }
}