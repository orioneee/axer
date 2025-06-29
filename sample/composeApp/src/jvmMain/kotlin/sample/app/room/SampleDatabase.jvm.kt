package sample.app.room

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

internal fun getDatabaseBuilder(): RoomDatabase.Builder<SampleDatabase> {
    val dbFile = File(System.getProperty("java.io.tmpdir"), "SampleDatabase.db")
    return Room.databaseBuilder<SampleDatabase>(
        name = dbFile.absolutePath,
    )
}