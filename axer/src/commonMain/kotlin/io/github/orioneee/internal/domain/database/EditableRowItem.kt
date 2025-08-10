package io.github.orioneee.internal.domain.database

import kotlinx.serialization.Serializable

@Serializable
data class EditableRowItem(
    val schemaItem: SchemaItem,
    val item: RowItem,
    val selectedColumnIndex: Int,
    val editedValue: RoomCell?,
)