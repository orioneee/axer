package com.oriooneee.axer.domain.database

import com.oriooneee.axer.room.RoomReader

internal data class RoomCell(
    val type: RoomReader.SQLiteColumnType,
    val value: String,
)