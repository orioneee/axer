package io.github.orioneee.internal.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.orioneee.internal.extentions.formate
import io.github.orioneee.internal.presentation.screens.database.TableDetailsViewModel

@Composable
internal fun HeaderCell(
    text: String?,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    onClick: (() -> Unit)? = null,
    isSortColumn: Boolean,
    isDescending: Boolean = false
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
        page * TableDetailsViewModel.PAGE_SIZE
    }
    val lastVisibleItemIndex = remember(firstVisibleItemIndex, currentItemsSize) {
        firstVisibleItemIndex + currentItemsSize - 1
    }
    val canMinusPage = remember(page) {
        page > 0
    }
    val canPlusPage = remember(page, totalItems) {
        (page + 1) * TableDetailsViewModel.PAGE_SIZE < totalItems
    }
//    if (totalItems > 1) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {

        var rowCountTarget = remember { mutableStateOf(totalItems) }

        val animatedRowCount = animateIntAsState(rowCountTarget.value)

        LaunchedEffect(totalItems) {
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
                onSetPage(page + 1)
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
                val lastPage = totalItems.toFloat().div(TableDetailsViewModel.PAGE_SIZE)
                //round to biggest integer
                onSetPage(
                    if (lastPage % 1 == 0f) {
                        lastPage.toInt() - 1
                    } else {
                        lastPage.toInt()
                    }
                )
//                    onSetPage()
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

//        }
    }
}

@Composable
private fun Table(
    modifier: Modifier = Modifier,
    rowCount: Int,
    columnCount: Int,
    maxCellWidthDp: Dp = Dp.Infinity,
    maxCellHeightDp: Dp = Dp.Infinity,
    cellContent: @Composable (rowIndex: Int, columnIndex: Int) -> Unit
) {
    val columnWidths = remember { mutableStateMapOf<Int, Int>() }
    val rowHeights = remember { mutableStateMapOf<Int, Int>() }

    if (columnCount != columnWidths.size || rowCount != rowHeights.size) {
        columnWidths.clear()
        rowHeights.clear()
    }

    val maxCellWidth = if (listOf(Dp.Infinity, Dp.Unspecified).contains(maxCellWidthDp)) {
        Constraints.Infinity
    } else {
        with(LocalDensity.current) { maxCellWidthDp.toPx() }.toInt()
    }
    val maxCellHeight = if (listOf(Dp.Infinity, Dp.Unspecified).contains(maxCellHeightDp)) {
        Constraints.Infinity
    } else {
        with(LocalDensity.current) { maxCellHeightDp.toPx() }.toInt()
    }

    // not using mutableStateListOf because the list is entirely replaced on mutations
    var accumWidths by remember { mutableStateOf(listOf<Int>()) }
    var accumHeights by remember { mutableStateOf(listOf<Int>()) }

    @Composable
    fun StickyCells(modifier: Modifier = Modifier, rowCount: Int, columnCount: Int) {
        if (rowCount > 0 && columnCount > 0) {
            Box(modifier = modifier) {
                Layout(
                    content = {
                        (0 until rowCount).forEach { rowIndex ->
                            (0 until columnCount).forEach { columnIndex ->
                                cellContent(rowIndex, columnIndex)
                            }
                        }
                    },
                ) { measurables, constraints ->
                    val placeables = measurables.mapIndexed { index, it ->
                        val columnIndex = index % columnCount
                        val rowIndex = index / columnCount
                        it.measure(
                            Constraints(
                                minWidth = columnWidths[columnIndex] ?: 0,
                                minHeight = rowHeights[rowIndex] ?: 0,
                                maxWidth = columnWidths[columnIndex] ?: 0,
                                maxHeight = rowHeights[rowIndex] ?: 0
                            )
                        )
                    }

                    val totalWidth = accumWidths[columnCount]
                    val totalHeight = accumHeights[rowCount]

                    layout(width = totalWidth, height = totalHeight) {
                        placeables.forEachIndexed { index, placeable ->
                            val columnIndex = index % columnCount
                            val rowIndex = index / columnCount

                            placeable.placeRelative(
                                accumWidths[columnIndex],
                                accumHeights[rowIndex]
                            )
                        }
                    }
                }
            }
        }
    }

    Box(modifier = modifier) {
        Box(
        ) {
            Layout(
                content = {
                    (0 until rowCount).forEach { rowIndex ->
                        (0 until columnCount).forEach { columnIndex ->
                            cellContent(rowIndex, columnIndex)
                        }
                    }
                },
            ) { measurables, constraints ->
                val placeables = measurables.mapIndexed { index, it ->
                    val columnIndex = index % columnCount
                    val rowIndex = index / columnCount
                    it.measure(
                        Constraints(
                            minWidth = columnWidths[columnIndex] ?: 0,
                            minHeight = rowHeights[rowIndex] ?: 0,
                            maxWidth = maxCellWidth,
                            maxHeight = maxCellHeight
                        )
                    )
                }

                placeables.forEachIndexed { index, placeable ->
                    val columnIndex = index % columnCount
                    val rowIndex = index / columnCount

                    val existingWidth = columnWidths[columnIndex] ?: 0
                    val maxWidth = maxOf(existingWidth, placeable.width)
                    if (maxWidth > existingWidth || (existingWidth == 0 && maxWidth == existingWidth)) {
                        columnWidths[columnIndex] = maxWidth
                    }

                    val existingHeight = rowHeights[rowIndex] ?: 0
                    val maxHeight = maxOf(existingHeight, placeable.height)
                    if (maxHeight > existingHeight || (existingHeight == 0 && maxHeight == existingHeight)) {
                        rowHeights[rowIndex] = maxHeight
                    }
                }

                accumWidths = mutableListOf(0).apply {
                    (1..columnWidths.size).forEach { i ->
                        this += this.last() + columnWidths[i - 1]!!
                    }
                }
                accumHeights = mutableListOf(0).apply {
                    (1..rowHeights.size).forEach { i ->
                        this += this.last() + rowHeights[i - 1]!!
                    }
                }

                val totalWidth = accumWidths.last()
                val totalHeight = accumHeights.last()

                layout(width = totalWidth, height = totalHeight) {
                    placeables.forEachIndexed { index, placeable ->
                        val columnIndex = index % columnCount
                        val rowIndex = index / columnCount

                        placeable.placeRelative(accumWidths[columnIndex], accumHeights[rowIndex])
                    }
                }
            }
        }

        if (columnWidths.isEmpty() || rowHeights.isEmpty()) {
            return@Box
        }

        StickyCells(
            rowCount = 0,
            columnCount = columnCount
        )

        StickyCells(
            rowCount = rowCount,
            columnCount = 0
        )

        StickyCells(
            rowCount = 0,
            columnCount = 0
        )
    }
}

@Composable
internal fun <T, R> ViewTable(
    headers: List<T>,
    rows: List<R>,
    withDeleteButton: Boolean,
    headerUI: @Composable (T, Int, Int) -> Unit,
    cellUI: @Composable (R, T, Int, Int) -> Unit,
    deleteButtonUI: (@Composable (R, Int) -> Unit)? = null,
    deleteButtonHeaderUI: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
        .padding(bottom = 8.dp)
        .clip(RoundedCornerShape(8.dp))
        .border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline,
            shape = RoundedCornerShape(8.dp)
        ),
) {
    Column {
        val horizontalScroll = rememberScrollState()
        PlatformHorizontalScrollBar(horizontalScroll)
        Table(
            modifier = modifier
                .horizontalScroll(horizontalScroll),
            rowCount = rows.size + 1,
            columnCount = headers.size.let {
                if (withDeleteButton) {
                    it + 1
                } else {
                    it
                }
            },
        ) { rowIndex, columnIndex ->
            if (rowIndex == 0 && columnIndex < headers.size) {
                headerUI.invoke(
                    headers[columnIndex],
                    rowIndex,
                    columnIndex
                )
            } else if (rowIndex == 0 && columnIndex == headers.size && withDeleteButton) {
                deleteButtonHeaderUI?.invoke()
            } else if (rowIndex > 0 && columnIndex < headers.size) {
                cellUI.invoke(
                    rows[rowIndex - 1],
                    headers[columnIndex],
                    rowIndex,
                    columnIndex
                )
            } else {
                if (withDeleteButton) {
                    deleteButtonUI?.invoke(
                        rows[rowIndex - 1],
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
}