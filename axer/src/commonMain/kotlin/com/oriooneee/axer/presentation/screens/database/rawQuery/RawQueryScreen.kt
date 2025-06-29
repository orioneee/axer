package com.oriooneee.axer.presentation.screens.database.rawQuery

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.oriooneee.axer.presentation.components.ContentCell
import com.oriooneee.axer.presentation.components.HeaderCell
import com.oriooneee.axer.presentation.components.PaginationUI
import com.oriooneee.axer.presentation.components.ViewSQLTable
import com.oriooneee.axer.sortBySortingItemAndChunck
import org.koin.compose.viewmodel.koinViewModel

internal class RawQueryScreen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Screen(
        onBack: () -> Unit,
    ) {
        val viewModel: RawQueryViewModel = koinViewModel()
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
        val currentItems = pages.getOrNull(currentPage) ?: emptyList()
        val isLoading by viewModel.isLoading.collectAsState(false)
        val currentQuery by viewModel.currentQuery.collectAsState("")
        val sortColumn by viewModel.sortColumn.collectAsState()
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Raw query") },
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
                )
            }
        ) { contentPadding ->
            Column(
                modifier = Modifier.Companion
                    .fillMaxSize()
                    .padding(contentPadding)
                    .padding(8.dp),
                horizontalAlignment = Alignment.Companion.CenterHorizontally,
            ) {
                OutlinedTextField(
                    enabled = !isLoading,
                    trailingIcon = {
                        IconButton(
                            enabled = !isLoading,
                            onClick = {
                                viewModel.executeQuery()
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
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .padding(vertical = 16.dp),
                    value = currentQuery,
                    onValueChange = {
                        viewModel.setQuery(it)
                    },
                )
                PaginationUI(
                    pages = pages,
                    page = currentPage,
                    onSetPage = { newPage ->
                        currentPage = newPage
                    },
                    currentItemsSize = currentItems.size,
                )
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    ViewSQLTable(
                        headers = schema,
                        row = currentItems,
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
                            ContentCell(
                                text = cellData?.value ?: "NULL",
                                alignment = Alignment.CenterStart,
                                backgroundColor = when {
                                    isPrimary -> MaterialTheme.colorScheme.primaryContainer
                                    else -> MaterialTheme.colorScheme.surface
                                },
                                isClickable = false,
                                onClick = {},
                            )
                        },
                        deleteButtonUI = { line, rowIndex ->
                        },
                        deleteButtonHeaderUI = {
                        }
                    )
                }
            }
        }
    }
}