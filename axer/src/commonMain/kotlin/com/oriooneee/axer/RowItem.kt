package com.oriooneee.axer

import com.oriooneee.axer.domain.database.RowItem
import com.oriooneee.axer.domain.database.SortColumn
import com.oriooneee.axer.presentation.screens.database.DatabaseInspectionViewModel
import com.oriooneee.axer.room.RoomReader

internal fun List<RowItem>.sortBySortingItemAndChunck(
    sortColumn: SortColumn?,
    pageSize: Int = DatabaseInspectionViewModel.PAGE_SIZE
): List<List<RowItem>> {
    val reversed = this
        .reversed()

    val sorted = if (sortColumn != null) {
        if (sortColumn.isDescending) {
            if (sortColumn.schemaItem.type == RoomReader.SQLiteColumnType.INTEGER) {
                reversed.sortedByDescending {
                    it.cells[sortColumn.index]?.value?.toLongOrNull() ?: Long.MAX_VALUE
                }
            } else {
                reversed.sortedByDescending { it.cells[sortColumn.index]?.value ?: "" }
            }
        } else {
            if (sortColumn.schemaItem.type == RoomReader.SQLiteColumnType.INTEGER) {
                reversed.sortedBy {
                    it.cells[sortColumn.index]?.value?.toLongOrNull() ?: Long.MIN_VALUE
                }
            } else {
                reversed.sortedBy { it.cells[sortColumn.index]?.value ?: "" }
            }
        }
    } else {
        reversed
    }
    return sorted.chunked(pageSize)
}