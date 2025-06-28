package com.oriooneee.axer.presentation.screens.database

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.oriooneee.axer.domain.database.EditableRowItem
import io.github.windedge.table.m3.PaginatedDataTable
import io.github.windedge.table.rememberPaginationState
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

class TableDetails {
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
        val paginationState = rememberPaginationState(content.size, pageSize = 20)
        var selectedItem by remember { mutableStateOf<EditableRowItem?>(null) }
        LaunchedEffect(selectedItem) {
            println("Selected Item: $selectedItem")
        }
        Scaffold(
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
                    .verticalScroll(rememberScrollState())
            ) {
                if (schema.isEmpty()) return@Scaffold
                PaginatedDataTable(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState()),
                    columns = {
                        headerBackground {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(color = MaterialTheme.colorScheme.primaryContainer)
                            )
                        }
                        schema.forEach {
                            column(
                                contentAlignment = Alignment.Center
                            ) {
                                Text(it.name)
                            }
                        }
                    },
                    paginationState = paginationState,
                    onPageChanged = {
                        if (content.isNotEmpty()) {
                            content.chunked(it.pageSize)[it.pageIndex - 1]
                        } else {
                            emptyList()
                        }
                    }
                ) { data ->
                    row {
                        data.cells.forEachIndexed { index, item ->
                            cell(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier

                                    .padding(4.dp)
                                    .border(
                                        width = Dp.Hairline,
                                        color = if (selectedItem?.item == data.cells) Color.Red else Color.Blue,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable {
                                        selectedItem = if (selectedItem?.item == data.cells) null
                                        else EditableRowItem(data, index)
                                    }
                            ) {
                                Text(
                                    text = item,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .widthIn(max = 150.dp)

                                )
                            }
                        }
                    }
                }
            }
        }
    }
}