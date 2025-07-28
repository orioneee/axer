package io.github.orioneee.room

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.orioneee.domain.exceptions.AxerException
import io.github.orioneee.domain.logs.LogLine
import io.github.orioneee.room.converters.ListConverter
import io.github.orioneee.room.converters.MapConverter
import io.github.orioneee.room.dao.RequestDao
import io.github.orioneee.domain.requests.data.Transaction
import io.github.orioneee.domain.requests.data.TransactionFull
import io.github.orioneee.room.converters.LogLevelConverter
import io.github.orioneee.room.dao.AxerExceptionDao
import io.github.orioneee.room.dao.LogsDAO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(
    entities = [
        TransactionFull::class,
        AxerException::class,
        LogLine::class,
    ],
    version = 19
)
@ConstructedBy(AxerDatabaseConstructor::class)
@TypeConverters(MapConverter::class, ListConverter::class, LogLevelConverter::class)
internal abstract class AxerDatabase : RoomDatabase() {
    abstract fun getRequestDao(): RequestDao
    abstract fun getAxerExceptionDao(): AxerExceptionDao
    abstract fun getLogsDao(): LogsDAO
}

// Room compiler generates the `actual` implementations
@Suppress("NO_ACTUAL_FOR_EXPECT", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
internal expect object AxerDatabaseConstructor : RoomDatabaseConstructor<AxerDatabase> {
    override fun initialize(): AxerDatabase
}

internal fun getAxerDatabase(builder: RoomDatabase.Builder<AxerDatabase>): AxerDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
//        .setDriver(AxerBundledSQLiteDriver.getInstance())
        .setQueryCoroutineContext(Dispatchers.IO)
        .fallbackToDestructiveMigration(false)
        .build()
}