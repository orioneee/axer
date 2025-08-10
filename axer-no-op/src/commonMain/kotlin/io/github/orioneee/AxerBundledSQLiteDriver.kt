package io.github.orioneee

import androidx.sqlite.SQLiteDriver

internal expect fun getBaseSqliteDriver(): SQLiteDriver
class AxerBundledSQLiteDriver private constructor() {
    companion object {

        fun getInstance(): SQLiteDriver = getBaseSqliteDriver()
    }
}