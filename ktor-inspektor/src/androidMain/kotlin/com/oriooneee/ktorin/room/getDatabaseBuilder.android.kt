package com.oriooneee.ktorin.room

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<KtorinDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath("movie_database.db")

    return Room.databaseBuilder<KtorinDatabase>(
        context = appContext,
        name = dbFile.absolutePath,
    )
}