package io.github.orioneee

import androidx.sqlite.SQLiteDriver
import androidx.sqlite.driver.AndroidSQLiteDriver

internal actual fun getBaseSqliteDriver(): SQLiteDriver = AndroidSQLiteDriver()