package io.github.orioneee.presentation.screens.database.rawQuery

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import io.github.orioneee.axer.generated.resources.Res
import io.github.orioneee.axer.generated.resources.execute_query
import io.github.orioneee.domain.database.EditableRowItem
import io.github.orioneee.presentation.components.ContentCell
import io.github.orioneee.presentation.components.HeaderCell
import io.github.orioneee.presentation.components.PaginationUI
import io.github.orioneee.presentation.components.ViewTable
import io.github.orioneee.extentions.sortBySortingItemAndChunck
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

internal class RawQueryScreen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Screen(
        name: String,
        onBack: () -> Unit,
    ) {
        val viewModel: RawQueryViewModel = koinViewModel {
            parametersOf(name)
        }
        val queryResponse by viewModel.queryResponse.collectAsState()
        val sortingColumn by viewModel.sortColumn.collectAsState()
        val schema = remember(queryResponse) {
            queryResponse.schema
        }
        val pages = remember(queryResponse, sortingColumn) {
            queryResponse.rows.sortBySortingItemAndChunck(sortingColumn)
        }
        var currentPage by remember {
            mutableStateOf(0)
        }
        var selectedCellForPreview: EditableRowItem? by remember {
            mutableStateOf(null)
        }
        var isShowingPreview by remember {
            mutableStateOf(false)
        }
        val currentItems = pages.getOrNull(currentPage) ?: emptyList()
        val isLoading by viewModel.isLoading.collectAsState(false)
        val currentQuery by viewModel.currentQuery.collectAsState("")
        val sortColumn by viewModel.sortColumn.collectAsState()
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleSmall
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                )
            }) { contentPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .padding(horizontal = 8.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Companion.CenterHorizontally,
            ) {
                OutlinedTextField(
                    enabled = !isLoading,
                    trailingIcon = {
                        IconButton(
                            enabled = !isLoading, onClick = {
                                viewModel.executeQuery()
                            }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Save Changes",
                                modifier = Modifier.rotate(180f)
                            )
                        }
                    },
                    modifier = Modifier
                        .onPreviewKeyEvent { keyEvent ->
                            if (keyEvent.type == KeyEventType.KeyDown) {
                                // Only trigger if Enter is pressed alone (no modifiers like Shift)
                                if (keyEvent.key == Key.Enter && keyEvent.isCtrlPressed.not() && keyEvent.isShiftPressed.not() && keyEvent.isAltPressed.not() && keyEvent.isMetaPressed.not()) {
                                    viewModel.executeQuery()
                                    return@onPreviewKeyEvent true // consume the event
                                }
                            }
                            return@onPreviewKeyEvent false // let TextField handle all other keys
                        }

                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .padding(vertical = 16.dp),
                    value = currentQuery,
                    onValueChange = {
                        viewModel.setQuery(it)
                    },
                )
                PaginationUI(
                    totalItems = queryResponse.rows.size,
                    page = currentPage,
                    onSetPage = { newPage ->
                        currentPage = newPage
                    },
                    currentItemsSize = currentItems.size,
                )
                AnimatedVisibility(
                    isShowingPreview,
                ) {
                    DisposableEffect(Unit) {
                        onDispose {
                            selectedCellForPreview = null
                        }
                    }
                    SelectionContainer {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(16.dp)
                                )
                        ) {

                            Text(
                                selectedCellForPreview?.editedValue?.value ?: "",
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    ViewTable(
                        headers = schema,
                        rows = currentItems,
                        withDeleteButton = false,
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
                            val cellData = line.cells[columnIndex]
                            val isSelected = selectedCellForPreview?.let {
                                it.schemaItem == schema && it.selectedColumnIndex == columnIndex && it.item == line && it.editedValue == cellData && isShowingPreview
                            } ?: false
                            ContentCell(
                                text = cellData?.value ?: "NULL",
                                alignment = Alignment.CenterStart,
                                backgroundColor = when {
                                    isSelected -> MaterialTheme.colorScheme.tertiaryContainer
                                    isPrimary -> MaterialTheme.colorScheme.primaryContainer
                                    else -> MaterialTheme.colorScheme.surface
                                },
                                isClickable = true,
                                onClick = {
                                    if (isSelected) {
                                        isShowingPreview = false
                                    } else {
                                        selectedCellForPreview = EditableRowItem(
                                            schemaItem = schema,
                                            selectedColumnIndex = columnIndex,
                                            item = line,
                                            editedValue = cellData
                                        )
                                        isShowingPreview = true
                                    }
                                },
                            )
                        },
                    )
                }
            }
        }
    }
}