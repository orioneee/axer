package io.github.orioneee.room

import androidx.sqlite.SQLiteDriver
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

internal expect fun getBaseSqliteDriver(): SQLiteDriver

class AxerBundledSQLiteDriver private constructor() {
    companion object {
        fun getInstance(): SQLiteDriver = getBaseSqliteDriver()
    }
}