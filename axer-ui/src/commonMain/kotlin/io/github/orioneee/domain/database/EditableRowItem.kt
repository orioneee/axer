package io.github.orioneee.domain.database

data class EditableRowItem(
    val schemaItem: SchemaItem,
    val item: RowItem,
    val selectedColumnIndex: Int,
    val editedValue: RoomCell?,
)