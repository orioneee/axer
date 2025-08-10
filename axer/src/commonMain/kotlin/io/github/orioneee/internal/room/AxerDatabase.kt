package io.github.orioneee.internal.room

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import io.github.orioneee.getBaseSqliteDriver
import io.github.orioneee.internal.domain.exceptions.AxerException
import io.github.orioneee.internal.domain.logs.LogLine
import io.github.orioneee.internal.domain.requests.data.TransactionFull
import io.github.orioneee.internal.room.converters.ListConverter
import io.github.orioneee.internal.room.converters.LogLevelConverter
import io.github.orioneee.internal.room.converters.MapConverter
import io.github.orioneee.internal.room.dao.AxerExceptionDao
import io.github.orioneee.internal.room.dao.LogsDAO
import io.github.orioneee.internal.room.dao.RequestDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO


@Database(
    exportSchema = false,
    entities = [
        TransactionFull::class,
        AxerException::class,
        LogLine::class,
    ],
    version = 23
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
        .setDriver(getBaseSqliteDriver())
//        .setDriver(AxerBundledSQLiteDriver.getInstance())
        .setQueryCoroutineContext(Dispatchers.IO)
        .fallbackToDestructiveMigration(true)
        .build()
}