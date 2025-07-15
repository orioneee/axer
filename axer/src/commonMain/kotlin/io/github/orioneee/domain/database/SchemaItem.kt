package io.github.orioneee.domain.database

internal data class SchemaItem(
    val name: String,
    val isPrimary: Boolean,
    val isNullable: Boolean,
    val type: SQLiteColumnType,
)