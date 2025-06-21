package com.oriooneee.ktorin.room.converters

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json

class MapConverter {
    @TypeConverter
    fun fromMapToString(map: Map<String, String>?): String? {
        val string = Json.Default.encodeToString(map)
        return string
    }

    @TypeConverter
    fun fromStringToMap(string: String?): Map<String, String>? {
        return if (string.isNullOrEmpty()) {
            null
        } else {
            Json.Default.decodeFromString<Map<String, String>>(string)
        }
    }
}