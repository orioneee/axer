package io.github.orioneee.domain.database

import kotlinx.serialization.Serializable

@Serializable
internal data class Table(
    val name: String,
    val rowCount: Int,
    val columnCount: Int,
)