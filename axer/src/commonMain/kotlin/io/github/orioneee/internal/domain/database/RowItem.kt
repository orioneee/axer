package io.github.orioneee.internal.domain.database

import kotlinx.serialization.Serializable

@Serializable
data class RowItem (
    val schema: List<SchemaItem>,
    val cells: List<RoomCell?>
)