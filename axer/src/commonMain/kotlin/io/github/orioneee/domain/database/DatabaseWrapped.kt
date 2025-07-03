package io.github.orioneee.domain.database

internal data class DatabaseWrapped(
    val tables: List<Table>,
    val name: String,
)