package io.github.orioneee.room

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

internal fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<AxerDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath("axer_database.db")

    return Room.databaseBuilder<AxerDatabase>(
        context = appContext,
        name = dbFile.absolutePath,
    )
}