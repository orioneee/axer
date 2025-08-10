package io.github.orioneee.internal.domain.database

import kotlinx.serialization.Serializable

@Serializable
data class QueryResponse(
    val schema: List<SchemaItem>,
    val rows: List<RowItem>,
)