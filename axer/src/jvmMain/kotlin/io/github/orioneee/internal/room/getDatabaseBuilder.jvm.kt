package io.github.orioneee.internal.room

import androidx.room.Room
import androidx.room.RoomDatabase
import io.github.orioneee.internal.room.AxerDatabase
import java.io.File

internal fun getDatabaseBuilder(): RoomDatabase.Builder<AxerDatabase> {
    val dbFile = File("build/Axer", "AxerDatabase.db")
    return Room.databaseBuilder<AxerDatabase>(
        name = dbFile.absolutePath,
    )
}