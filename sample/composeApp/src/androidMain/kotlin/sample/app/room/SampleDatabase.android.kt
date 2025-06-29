package sample.app.room

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

internal fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<SampleDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath("SampleDatabase.db")

    return Room.databaseBuilder<SampleDatabase>(
        context = appContext,
        name = dbFile.absolutePath,
    )
}