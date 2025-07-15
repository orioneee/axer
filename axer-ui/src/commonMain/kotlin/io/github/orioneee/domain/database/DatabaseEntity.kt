package io.github.orioneee.domain.database

import androidx.sqlite.SQLiteConnection

data class DatabaseEntity(
    val file: String,
    val connection: SQLiteConnection
)