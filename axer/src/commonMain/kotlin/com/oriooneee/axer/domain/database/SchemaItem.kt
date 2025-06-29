package com.oriooneee.axer.domain.database

import com.oriooneee.axer.room.RoomReader

internal data class SchemaItem(
    val name: String,
    val isPrimary: Boolean,
    val isNullable: Boolean,
    val type: RoomReader.SQLiteColumnType,
)