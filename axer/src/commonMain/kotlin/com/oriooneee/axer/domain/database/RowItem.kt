package com.oriooneee.axer.domain.database

data class RowItem (
    val schema: List<SchemaItem>,
    val cells: List<String>
)