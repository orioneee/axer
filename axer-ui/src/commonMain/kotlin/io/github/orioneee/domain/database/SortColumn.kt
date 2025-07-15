package io.github.orioneee.domain.database

data class SortColumn(
    val index: Int,
    val schemaItem: SchemaItem,
    val isDescending: Boolean,
)