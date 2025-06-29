package com.oriooneee.axer.presentation.screens.database

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FirstPage
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.oriooneee.axer.domain.database.EditableRowItem
import com.oriooneee.axer.domain.database.SortColumn
import com.sunnychung.lib.android.composabletable.ux.Table
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

class TableDetails {

    @Composable
    fun HeaderCell(
        text: String?,
        backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
        onClick: (() -> Unit)? = null,
        isSortColumn: Boolean,
        isDescending: Boolean
    ) {
        if (text != null) {
            SelectionContainer {
                Box(
                    modifier = Modifier
                        .background(color = backgroundColor)
                        .widthIn(max = 350.dp)
                        .border(width = 1.dp, color = Color.Gray)
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
    fun ContentCell(
        text: String,
        isClickable: Boolean,
        alignment: Alignment = Alignment.Center,
        backgroundColor: Color = MaterialTheme.colorScheme.surface,
        onClick: () -> Unit = {}
    ) {
        Box(
            modifier = Modifier
                .background(color = backgroundColor)
                .widthIn(max = 350.dp)
                .border(width = 1.dp, color = Color.Gray)
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

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Screen(
        tableName: String,
        onBack: () -> Unit,
    ) {
        val viewModel: DatabaseInspectionViewModel = koinViewModel {
            parametersOf(tableName)
        }
        LaunchedEffect(Unit) {
            viewModel.getTableInfo()
        }
        val schema by viewModel.tableSchema.collectAsState()
        val pages by viewModel.tableContent.collectAsState(emptyList())
        val isUpdatingCell by viewModel.isUpdatingCell.collectAsState(false)
        val selectedItem by viewModel.editableRowItem.collectAsState()
        val message by viewModel.message.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        var currentPage by remember { mutableStateOf(0) }
        val currentItems = pages.getOrNull(currentPage) ?: emptyList()
        val sortColumn by viewModel.sortColumn.collectAsState()

        val firstVisibleItemIndex = remember(currentPage) {
            currentPage * DatabaseInspectionViewModel.PAGE_SIZE
        }
        val lastVisibleItemIndex = remember(firstVisibleItemIndex, currentItems) {
            firstVisibleItemIndex + currentItems.size - 1
        }
        val totalItems = remember(pages) {
            pages.sumOf { it.size }
        }
        val canMinusPage = remember(currentPage) {
            currentPage > 0
        }
        val canPlusPage = remember(currentPage, pages) {
            currentPage < pages.size - 1
        }


        LaunchedEffect(message) {
            message?.let {
                snackbarHostState.showSnackbar(it)
                viewModel.onHandledError()
            }
        }
        LaunchedEffect(currentPage) {
            viewModel.onSelectItem(null)
        }
        Scaffold(
            snackbarHost = {
                SnackbarHost(snackbarHostState)
            },
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Table - $tableName") },
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        TextButton(
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            onClick = {
                                viewModel.clearTable()
                            }
                        ) {
                            Text("Clear")
                        }
                    }
                )
            }
        ) { contentPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AnimatedVisibility(
                    selectedItem != null
                ) {
                    TextField(
                        enabled = !isUpdatingCell,
                        trailingIcon = {
                            IconButton(
                                enabled = !isUpdatingCell,
                                onClick = {
                                    selectedItem?.let { viewModel.updateCell(it) }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Save Changes",
                                    modifier = Modifier.rotate(180f)
                                )
                            }
                        },
                        modifier = Modifier
                            .heightIn(max = 400.dp)
                            .padding(vertical = 16.dp),
                        value = selectedItem?.editedValue?.value ?: "",
                        onValueChange = {
                            val newCell =
                                selectedItem?.editedValue?.copy(value = it) ?: return@TextField
                            viewModel.onEditableItemChanged(selectedItem?.copy(editedValue = newCell))
                        },
                        leadingIcon = if (selectedItem?.schemaItem?.isNullable == true) {
                            {
                                TextButton(
                                    onClick = {
                                        selectedItem?.let {
                                            viewModel.updateCell(it.copy(editedValue = null))
                                        }
                                    }
                                ) {
                                    Text("NULL")
                                }
                            }
                        } else {
                            null
                        }
                    )
                }
                if (pages.size > 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text("${firstVisibleItemIndex + 1} - ${lastVisibleItemIndex + 1} of $totalItems")
                        IconButton(
                            enabled = canMinusPage,
                            onClick = {
                                currentPage = 0
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
                                currentPage = (currentPage - 1).coerceAtLeast(0)
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
                                currentPage = (currentPage + 1).coerceAtMost(pages.size - 1)
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
                                currentPage = pages.lastIndex
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
                Table(
                    rowCount = currentItems.size + 1, // +1 for header
                    columnCount = schema.size + 1,    // +1 for delete button column at the end
                    stickyRowCount = 1,
                    stickyColumnCount = 1
                ) { rowIndex, columnIndex ->
                    if (rowIndex == 0) {
                        if (columnIndex < schema.size) {
                            HeaderCell(
                                text = schema[columnIndex].name,
                                onClick = {
                                    viewModel.onClickSortColumn(schema[columnIndex])
                                },
                                isSortColumn = sortColumn?.schemaItem == schema[columnIndex],
                                isDescending = sortColumn?.let {
                                    it.index == columnIndex && it.isDescending
                                } == true,
                            )
                        } else {
                            HeaderCell(
                                text = null,
                                backgroundColor = MaterialTheme.colorScheme.surface,
                                isSortColumn = false,
                                isDescending = false,
                            )
                        }
                    } else {
                        val item = currentItems[rowIndex - 1]
                        if (columnIndex < schema.size) {
                            val isPrimary = schema[columnIndex].isPrimary
                            val isSelected =
                                selectedItem?.item == item && selectedItem?.selectedColumnIndex == columnIndex
                            val cellData = item.cells[columnIndex]
                            ContentCell(
                                text = cellData?.value ?: "NULL",
                                alignment = Alignment.CenterStart,
                                backgroundColor = when {
                                    isSelected -> MaterialTheme.colorScheme.tertiaryContainer
                                    isPrimary -> MaterialTheme.colorScheme.primaryContainer
                                    else -> MaterialTheme.colorScheme.surface
                                },
                                isClickable = !isPrimary && !isUpdatingCell,
                                onClick = {
                                    val newSelectedItem = if (!isSelected && cellData != null) {
                                        EditableRowItem(
                                            item = item,
                                            selectedColumnIndex = columnIndex,
                                            editedValue = cellData,
                                            schemaItem = schema[columnIndex]
                                        )
                                    } else {
                                        null
                                    }
                                    viewModel.onSelectItem(newSelectedItem)
                                }
                            )
                        } else {
                            // Last column = delete button
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(MaterialTheme.colorScheme.surface)
                            ) {
                                IconButton(
                                    onClick = {
                                        viewModel.deleteRow(item)
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Row",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}