package com.oriooneee.axer.presentation.screens.database

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.oriooneee.axer.domain.database.EditableRowItem
import com.sunnychung.lib.android.composabletable.ux.Table
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

class TableDetails {

    @Composable
    fun HeaderCell(
        text: String?,
        backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        if (text != null) {
            SelectionContainer {
                Box(
                    modifier = Modifier
                        .background(color = backgroundColor)
                        .widthIn(max = 350.dp)
                        .border(width = 1.dp, color = Color.Gray)
                ) {
                    Text(
                        text = text,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(10.dp)
                            .align(Alignment.Center),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
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
        val content by viewModel.tableContent.collectAsState()
        val isUpdatingCell by viewModel.isUpdatingCell.collectAsState(false)
        val selectedItem by viewModel.editableRowItem.collectAsState()
        val message by viewModel.message.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        LaunchedEffect(message) {
            message?.let {
                snackbarHostState.showSnackbar(it)
                viewModel.onHandledError()
            }
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
                        }
                    )
                }
                Table(
                    rowCount = content.size + 1, // +1 for header
                    columnCount = schema.size + 1, // +1 for delete button column
                    stickyRowCount = 1,
                    stickyColumnCount = 2
                ) { rowIndex, columnIndex ->
                    if (rowIndex == 0) {
                        // Header row
                        if (columnIndex == 0) {
                            HeaderCell(
                                text = null,
                                backgroundColor = MaterialTheme.colorScheme.surface
                            )
                        } else {
                            HeaderCell(
                                text = schema[columnIndex - 1].name
                            )
                        }
                    } else {
                        val item = content[rowIndex - 1]
                        if (columnIndex == 0) {
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
                        } else {
                            val isPrimary = schema[columnIndex - 1].isPrimary
                            val isSelected =
                                selectedItem?.item == item && selectedItem?.selectedColumnIndex == (columnIndex - 1)
                            val cellData = item.cells[columnIndex - 1]
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
                                            selectedColumnIndex = columnIndex - 1,
                                            editedValue = cellData
                                        )
                                    } else {
                                        null
                                    }
                                    viewModel.onSelectItem(newSelectedItem)
                                }
                            )
                        }
                    }
                }

            }
        }
    }
}