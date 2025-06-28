package com.oriooneee.axer.room

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.oriooneee.axer.domain.exceptions.AxerException
import com.oriooneee.axer.room.converters.ListConverter
import com.oriooneee.axer.room.converters.MapConverter
import com.oriooneee.axer.room.dao.RequestDao
import com.oriooneee.axer.domain.requests.Transaction
import com.oriooneee.axer.room.dao.AxerExceptionDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(
    entities = [
        Transaction::class,
        AxerException::class
    ],
    version = 10
)
@ConstructedBy(AxerDatabaseConstructor::class)
@TypeConverters(MapConverter::class, ListConverter::class)
internal abstract class AxerDatabase : RoomDatabase() {
    abstract fun getRequestDao(): RequestDao
    abstract fun getAxerExceptionDao(): AxerExceptionDao
}

// Room compiler generates the `actual` implementations
@Suppress("NO_ACTUAL_FOR_EXPECT", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
internal expect object AxerDatabaseConstructor : RoomDatabaseConstructor<AxerDatabase> {
    override fun initialize(): AxerDatabase
}

internal fun getAxerDatabase(builder: RoomDatabase.Builder<AxerDatabase>): AxerDatabase {
    return builder
        .setDriver(AxerBundledSQLiteDriver.getInstance())
        .setQueryCoroutineContext(Dispatchers.IO)
        .fallbackToDestructiveMigration(false)
        .build()
}