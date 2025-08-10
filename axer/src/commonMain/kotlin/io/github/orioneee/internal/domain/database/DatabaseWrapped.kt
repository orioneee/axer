package io.github.orioneee.internal.domain.database

import kotlinx.serialization.Serializable

@Serializable
data class DatabaseWrapped(
    val tables: List<Table>,
    val name: String,
)