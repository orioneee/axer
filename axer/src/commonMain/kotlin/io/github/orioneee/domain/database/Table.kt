package io.github.orioneee.domain.database

internal data class Table(
    val name: String,
    val rowCount: Int,
    val columnCount: Int,
)