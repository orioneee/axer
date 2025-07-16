package io.github.orioneee.domain.database

import kotlinx.serialization.Serializable

@Serializable
data class DatabaseWrapped(
    val tables: List<Table>,
    val name: String,
)