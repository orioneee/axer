package com.oriooneee.axer.room

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.oriooneee.axer.room.converters.ListConverter
import com.oriooneee.axer.room.converters.MapConverter
import com.oriooneee.axer.room.dao.RequestDao
import com.oriooneee.axer.domain.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(
    entities = [
        Transaction::class
    ],
    version = 7
)
@ConstructedBy(AxerDatabaseConstructor::class)
@TypeConverters(MapConverter::class, ListConverter::class)
abstract class AxerDatabase : RoomDatabase() {
    abstract fun getRequestDao(): RequestDao
}

// Room compiler generates the `actual` implementations
@Suppress("NO_ACTUAL_FOR_EXPECT", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object AxerDatabaseConstructor : RoomDatabaseConstructor<AxerDatabase> {
    override fun initialize(): AxerDatabase
}

fun getAxerDatabase(builder: RoomDatabase.Builder<AxerDatabase>): AxerDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .fallbackToDestructiveMigration(false)
        .build()
}