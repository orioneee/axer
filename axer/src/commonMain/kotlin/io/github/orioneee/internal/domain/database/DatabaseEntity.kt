package io.github.orioneee.internal.domain.database

import androidx.sqlite.SQLiteConnection

internal data class DatabaseEntity(
    val file: String,
    val connection: SQLiteConnection
)