package com.oriooneee.ktorin.room

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

fun getDatabaseBuilder(): RoomDatabase.Builder<KtorinDatabase> {
    val dbFile = File(System.getProperty("java.io.tmpdir"), "ktorinDatabase.db")
    return Room.databaseBuilder<KtorinDatabase>(
        name = dbFile.absolutePath,
    )
}