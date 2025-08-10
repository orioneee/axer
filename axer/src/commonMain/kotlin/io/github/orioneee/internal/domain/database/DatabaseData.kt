package io.github.orioneee.internal.domain.database

import kotlinx.serialization.Serializable

@Serializable
data class DatabaseData(
    val schema: List<SchemaItem>,
    val data: List<List<RoomCell?>>,
    val totalItems: Int,
)