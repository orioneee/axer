package io.github.orioneee.internal.domain.database

import kotlinx.serialization.Serializable

@Serializable
data class SchemaItem(
    val name: String,
    val isPrimary: Boolean,
    val isNullable: Boolean,
    val type: SQLiteColumnType,
)