package com.oriooneee.axer.room

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<AxerDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath("movie_database.db")

    return Room.databaseBuilder<AxerDatabase>(
        context = appContext,
        name = dbFile.absolutePath,
    )
}