package com.oriooneee.ktorin.room

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.oriooneee.ktorin.room.converters.ListConverter
import com.oriooneee.ktorin.room.converters.MapConverter
import com.oriooneee.ktorin.room.dao.RequestDao
import com.oriooneee.ktorin.room.entities.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(
    entities = [
        Transaction::class
    ],
    version = 6
)
@ConstructedBy(KtorinDatabaseConstructor::class)
@TypeConverters(MapConverter::class, ListConverter::class)
abstract class KtorinDatabase : RoomDatabase() {
    abstract fun getRequestDao(): RequestDao
}

// Room compiler generates the `actual` implementations
@Suppress("NO_ACTUAL_FOR_EXPECT", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object KtorinDatabaseConstructor : RoomDatabaseConstructor<KtorinDatabase> {
    override fun initialize(): KtorinDatabase
}

fun getKtorinDatabase(builder: RoomDatabase.Builder<KtorinDatabase>): KtorinDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .fallbackToDestructiveMigration(false)
        .build()
}