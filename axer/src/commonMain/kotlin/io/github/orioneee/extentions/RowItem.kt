package io.github.orioneee.extentions

import io.github.orioneee.domain.database.RowItem
import io.github.orioneee.domain.database.SQLiteColumnType
import io.github.orioneee.domain.database.SortColumn
import io.github.orioneee.presentation.screens.database.TableDetailsViewModel
import io.github.orioneee.processors.RoomReader

internal fun List<RowItem>.sortBySortingItemAndChunck(
    sortColumn: SortColumn?,
    pageSize: Int = TableDetailsViewModel.PAGE_SIZE
) = sortBySortingItem(sortColumn).chunked(pageSize)


internal fun List<RowItem>.sortBySortingItem(
    sortColumn: SortColumn?,
): List<RowItem> {
    val reversed = this
        .reversed()

    val sorted = if (sortColumn != null) {
        if (sortColumn.isDescending) {
            if (sortColumn.schemaItem.type == SQLiteColumnType.INTEGER) {
                reversed.sortedByDescending {
                    it.cells[sortColumn.index]?.value?.toLongOrNull() ?: Long.MAX_VALUE
                }
            } else {
                reversed.sortedByDescending { it.cells[sortColumn.index]?.value ?: "" }
            }
        } else {
            if (sortColumn.schemaItem.type == SQLiteColumnType.INTEGER) {
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
    return sorted
}