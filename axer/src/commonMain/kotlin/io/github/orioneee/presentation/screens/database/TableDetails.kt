package io.github.orioneee.presentation.screens.database

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.RawOn
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import io.github.orioneee.axer.generated.resources.Res
import io.github.orioneee.axer.generated.resources.action
import io.github.orioneee.axer.generated.resources.clear
import io.github.orioneee.domain.database.EditableRowItem
import io.github.orioneee.presentation.LocalAxerDataProvider
import io.github.orioneee.presentation.components.ContentCell
import io.github.orioneee.presentation.components.DeleteButton
import io.github.orioneee.presentation.components.HeaderCell
import io.github.orioneee.presentation.components.PaginationUI
import io.github.orioneee.presentation.components.ViewTable
import io.github.orioneee.presentation.navigation.Routes
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

internal class TableDetails {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Screen(
        name: String,
        tableName: String,
        navController: NavHostController,
    ) {
        val provider = LocalAxerDataProvider.current
        val viewModel: TableDetailsViewModel = koinViewModel {
            parametersOf(provider, name, tableName)
        }
        val schema by viewModel.tableSchema.collectAsState()
        val isUpdatingCell by viewModel.isUpdatingCell.collectAsState(false)
        val selectedItem by viewModel.editableRowItem.collectAsState()
        val message by viewModel.message.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        val currentPage = viewModel.currentPage.collectAsState(0)
        val currentItems = viewModel.tableContent.collectAsState(emptyList())
        val sortColumn by viewModel.sortColumn.collectAsState()
        val totalItems = viewModel.totalItems.collectAsState()

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
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = {
                SnackbarHost(snackbarHostState)
            },
            topBar = {
                CenterAlignedTopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = {
                        Text(
                            "$name - $tableName",
                            style = MaterialTheme.typography.titleSmall
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                navController.popBackStack()
                            },
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                navController.navigate(Routes.RAW_QUERY.route + "/$name")
                            }
                        ) {
                            Icon(
                                Icons.Outlined.RawOn,
                                null,
                            )
                        }
                        TextButton(
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            onClick = {
                                viewModel.clearTable()
                            }
                        ) {
                            Text(stringResource(Res.string.clear))
                        }
                    }
                )
            }
        ) { contentPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .padding(horizontal = 8.dp)
                    .verticalScroll(rememberScrollState()),
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
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
                                selectedItem?.editedValue?.copy(value = it)
                                    ?: return@TextField
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
                PaginationUI(
                    totalItems = totalItems.value,
                    page = currentPage.value,
                    onSetPage = { newPage ->
                        viewModel.setPage(newPage)
                    },
                    currentItemsSize = currentItems.value.size,
                )
                ViewTable(
                    headers = schema,
                    rows = currentItems.value,
                    withDeleteButton = true,
                    headerUI = { item, rowIndex, columnIndex ->
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
                    },
                    cellUI = { line, schema, rowIndex, columnIndex ->
                        val isPrimary = schema.isPrimary
                        val isSelected =
                            selectedItem?.item == line && selectedItem?.selectedColumnIndex == columnIndex
                        val cellData = line.cells[columnIndex]
                        ContentCell(
                            text = cellData?.value ?: "NULL",
                            alignment = Alignment.CenterStart,
                            backgroundColor = when {
                                isSelected -> MaterialTheme.colorScheme.tertiaryContainer
                                isPrimary -> MaterialTheme.colorScheme.primaryContainer
                                else -> MaterialTheme.colorScheme.surface
                            },
                            isClickable = !isPrimary && !isUpdatingCell,
                            borderColor = if (isSelected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.outline,
                            onClick = {
                                val newSelectedItem = if (!isSelected && cellData != null) {
                                    EditableRowItem(
                                        item = line,
                                        selectedColumnIndex = columnIndex,
                                        editedValue = cellData,
                                        schemaItem = schema
                                    )
                                } else {
                                    null
                                }
                                viewModel.onSelectItem(newSelectedItem)
                            }
                        )
                    },
                    deleteButtonUI = { line, rowIndex ->
                        DeleteButton(
                            isClickable = !isUpdatingCell,
                            onClick = {
                                viewModel.deleteRow(line)
                            }
                        )
                    },
                    deleteButtonHeaderUI = {
                        HeaderCell(
                            text = stringResource(Res.string.action),
                            isSortColumn = false,
                            isDescending = false,
                        )
                    }
                )
            }
        }
    }
}