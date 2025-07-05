package io.github.orioneee.room

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

internal fun getDatabaseBuilder(): RoomDatabase.Builder<AxerDatabase> {
    val dbFile = File("Axer", "AxerDatabase.db")
    return Room.databaseBuilder<AxerDatabase>(
        name = dbFile.absolutePath,
    )
}