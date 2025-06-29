package sample.app.room

import androidx.room.RoomDatabase

import androidx.room.Room
import com.oriooneee.axer.room.AxerDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask


internal fun getDatabaseBuilder(): RoomDatabase.Builder<SampleDatabase> {
    val dbFilePath = documentDirectory() + "/sampleDatabase.db"
    return Room.databaseBuilder<SampleDatabase>(
        name = dbFilePath,
    )
}

@OptIn(ExperimentalForeignApi::class)
private fun documentDirectory(): String {
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    return requireNotNull(documentDirectory?.path)
}