package sample.app.room

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import io.github.orioneee.room.AxerBundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import sample.app.room.dao.DirectorDao
import sample.app.room.dao.MovieDao
import sample.app.room.entity.Director
import sample.app.room.entity.Movie

@Database(
    entities = [
        Movie::class,
        Director::class
    ],
    version = 1
)
@ConstructedBy(SampleDatabaseConstructor::class)
internal abstract class SampleDatabase : RoomDatabase() {
    abstract fun getMovieDao(): MovieDao
    abstract fun getDirectorDao(): DirectorDao
}

// Room compiler generates the `actual` implementations
@Suppress("NO_ACTUAL_FOR_EXPECT", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
internal expect object SampleDatabaseConstructor : RoomDatabaseConstructor<SampleDatabase> {
    override fun initialize(): SampleDatabase
}

internal fun getSampleDatabase(builder: RoomDatabase.Builder<SampleDatabase>): SampleDatabase {
    return builder
        .setDriver(AxerBundledSQLiteDriver.getInstance())
        .setQueryCoroutineContext(Dispatchers.IO)
        .fallbackToDestructiveMigration(false)
        .build()
}