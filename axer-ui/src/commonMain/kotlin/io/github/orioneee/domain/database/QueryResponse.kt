package io.github.orioneee.domain.database

data class QueryResponse(
    val schema: List<SchemaItem>,
    val rows: List<RowItem>,
)