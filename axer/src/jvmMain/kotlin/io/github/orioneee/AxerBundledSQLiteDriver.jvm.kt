package io.github.orioneee

import androidx.sqlite.SQLiteDriver
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

internal actual fun getBaseSqliteDriver(): SQLiteDriver = BundledSQLiteDriver()