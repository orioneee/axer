package io.github.orioneee.internal.domain.database

internal data class SortColumn(
    val index: Int,
    val schemaItem: SchemaItem,
    val isDescending: Boolean,
)