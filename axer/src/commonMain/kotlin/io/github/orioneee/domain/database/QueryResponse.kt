package io.github.orioneee.domain.database

internal data class QueryResponse(
    val schema: List<SchemaItem>,
    val rows: List<RowItem>,
)