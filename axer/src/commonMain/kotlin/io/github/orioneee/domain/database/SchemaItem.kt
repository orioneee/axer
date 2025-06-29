package io.github.orioneee.domain.database

import io.github.orioneee.room.RoomReader

internal data class SchemaItem(
    val name: String,
    val isPrimary: Boolean,
    val isNullable: Boolean,
    val type: RoomReader.SQLiteColumnType,
)