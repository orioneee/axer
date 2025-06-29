package io.github.orioneee.room

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

internal fun getDatabaseBuilder(): RoomDatabase.Builder<AxerDatabase> {
    val dbFile = File(System.getProperty("java.io.tmpdir"), "AxerDatabase.db")
    return Room.databaseBuilder<AxerDatabase>(
        name = dbFile.absolutePath,
    )
}