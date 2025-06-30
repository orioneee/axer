package io.github.orioneee.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FirstPage
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.orioneee.domain.database.RowItem
import io.github.orioneee.domain.database.SchemaItem
import io.github.orioneee.presentation.screens.database.DatabaseInspectionViewModel
import com.sunnychung.lib.android.composabletable.ux.Table
import io.github.orioneee.extentions.formate

@Composable
internal fun HeaderCell(
    text: String?,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    onClick: (() -> Unit)? = null,
    isSortColumn: Boolean,
    isDescending: Boolean
) {
    if (text != null) {
        Box(
            modifier = Modifier
                .background(color = backgroundColor)
                .widthIn(max = 350.dp)
                .border(width = .5.dp, color = MaterialTheme.colorScheme.outline)
                .clickable(
                    enabled = onClick != null
                ) {
                    onClick?.invoke()
                },
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = text,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(10.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val animatedRotation by animateFloatAsState(
                    targetValue = if (isDescending) 270f else 90f,
                    label = "SortIconRotation"
                )
                if (isSortColumn) {
                    Icon(
                        Icons.Outlined.ArrowBackIosNew,
                        contentDescription = "Sort Icon",
                        modifier = Modifier
                            .padding(8.dp)
                            .size(16.dp)
                            .rotate(animatedRotation)
                    )
                }
            }
        }
    } else {
        Spacer(
            Modifier
                .background(color = backgroundColor)
                .size(32.dp)
        )
    }
}

@Composable
internal fun ContentCell(
    text: String,
    isClickable: Boolean,
    alignment: Alignment = Alignment.Center,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    onClick: () -> Unit = {}
) {
    val animatedBackgroundColor by animateColorAsState(backgroundColor)
    val animatedBorderColor by animateColorAsState(borderColor)
    Box(
        modifier = Modifier
            .background(color = animatedBackgroundColor)
            .widthIn(max = 350.dp)
            .border(width = 0.5.dp, color = animatedBorderColor)
            .clickable(
                enabled = isClickable
            ) {
                onClick()
            }
    ) {
        Text(
            text = text,
            modifier = Modifier
                .padding(10.dp)
                .align(alignment),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
internal fun DeleteButton(
    isClickable: Boolean,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .background(color = backgroundColor)
            .widthIn(max = 350.dp)
            .border(width = .5.dp, color = MaterialTheme.colorScheme.outline),
        contentAlignment = Alignment.Center

    ) {
        IconButton(
            enabled = isClickable,
            onClick = onClick
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete Row",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
internal fun PaginationUI(
    totalItems: Int,
    page: Int,
    currentItemsSize: Int,
    onSetPage: (Int) -> Unit
) {
    val firstVisibleItemIndex = remember(page) {
        page * DatabaseInspectionViewModel.PAGE_SIZE
    }
    val lastVisibleItemIndex = remember(firstVisibleItemIndex, currentItemsSize) {
        firstVisibleItemIndex + currentItemsSize - 1
    }
    val canMinusPage = remember(page) {
        page > 0
    }
    val canPlusPage = remember(page, totalItems) {
        page < totalItems - 1
    }
    if (totalItems > 1) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {

            var rowCountTarget = remember { mutableStateOf(totalItems) }

            val animatedRowCount = animateIntAsState(rowCountTarget.value)

            LaunchedEffect(totalItems){
                rowCountTarget.value = totalItems
            }

            Text("${firstVisibleItemIndex + 1} - ${lastVisibleItemIndex + 1} of ${animatedRowCount.value.formate()}")
            IconButton(
                enabled = canMinusPage,
                onClick = {
                    onSetPage(0)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.FirstPage,
                    contentDescription = "First Page",
                    modifier = Modifier
                        .size(16.dp)
                )
            }
            IconButton(
                enabled = canMinusPage,
                onClick = {
                    onSetPage((page - 1).coerceAtLeast(0))
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = "First Page",
                    modifier = Modifier
                        .size(16.dp)
                )
            }
            IconButton(
                enabled = canPlusPage,
                onClick = {
                    onSetPage((page + 1).coerceAtMost(totalItems - 1))
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = "First Page",
                    modifier = Modifier
                        .rotate(180f)
                        .size(16.dp)
                )
            }
            IconButton(
                enabled = canPlusPage,
                onClick = {
                    onSetPage(totalItems - 1)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.FirstPage,
                    contentDescription = "First Page",
                    modifier = Modifier
                        .rotate(180f)
                        .size(16.dp)
                )
            }

        }
    }
}

@Composable
internal fun ViewSQLTable(
    headers: List<SchemaItem>,
    row: List<RowItem>,
    withDeleteButton: Boolean,
    headerUI: @Composable (SchemaItem, Int, Int) -> Unit,
    cellUI: @Composable (RowItem, SchemaItem, Int, Int) -> Unit,
    deleteButtonUI: @Composable (RowItem, Int) -> Unit,
    deleteButtonHeaderUI: @Composable () -> Unit,
    stickyRowCount: Int = 1,
    stickyColumnCount: Int = 1,
) {
    Table(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(8.dp)
            ),
        rowCount = row.size + 1,
        columnCount = headers.size.let {
            if (withDeleteButton) {
                it + 1
            } else {
                it
            }
        },
        stickyRowCount = stickyRowCount,
        stickyColumnCount = stickyColumnCount
    ) { rowIndex, columnIndex ->
        if (rowIndex == 0 && columnIndex < headers.size) {
            headerUI.invoke(
                headers[columnIndex],
                rowIndex,
                columnIndex
            )
        } else if (rowIndex == 0 && columnIndex == headers.size && withDeleteButton) {
            deleteButtonHeaderUI.invoke()
        } else if (rowIndex > 0 && columnIndex < headers.size) {
            cellUI.invoke(
                row[rowIndex - 1],
                headers[columnIndex],
                rowIndex,
                columnIndex
            )
        } else {
            if (withDeleteButton) {
                deleteButtonUI.invoke(
                    row[rowIndex - 1],
                    rowIndex - 1
                )
            } else {
                Box(
                    modifier = Modifier
                        .background(color = MaterialTheme.colorScheme.surface)
                        .padding(8.dp)
                )
            }
        }
    }
}