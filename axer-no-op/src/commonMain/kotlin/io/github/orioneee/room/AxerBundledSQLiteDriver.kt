package io.github.orioneee.room

import androidx.sqlite.SQLiteDriver
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

class AxerBundledSQLiteDriver private constructor() {
    companion object {
        fun getInstance(): SQLiteDriver = BundledSQLiteDriver()
    }
}