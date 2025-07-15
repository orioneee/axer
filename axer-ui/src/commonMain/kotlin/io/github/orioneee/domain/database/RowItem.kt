package io.github.orioneee.domain.database

data class RowItem (
    val schema: List<SchemaItem>,
    val cells: List<RoomCell?>
)