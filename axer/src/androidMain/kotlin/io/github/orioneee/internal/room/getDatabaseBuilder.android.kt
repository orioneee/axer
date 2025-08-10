package io.github.orioneee.internal.room

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import io.github.orioneee.internal.room.AxerDatabase

internal fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<AxerDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath("axer_database.db")

    return Room.databaseBuilder<AxerDatabase>(
        context = appContext,
        name = dbFile.absolutePath,
    )
}