package com.oriooneee.axer.domain.database

internal data class EditableRowItem(
    val schemaItem: SchemaItem,
    val item: RowItem,
    val selectedColumnIndex: Int,
    val editedValue: RoomCell?,
)