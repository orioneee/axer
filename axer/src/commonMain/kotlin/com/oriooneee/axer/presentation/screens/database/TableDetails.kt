package com.oriooneee.axer.presentation.screens.database

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.windedge.table.DataTable
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
            ) {
                if (schema.isEmpty()) return@Scaffold
                PaginatedDataTable(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .horizontalScroll(rememberScrollState()),
                    columns = {
                        headerBackground {
                            Box(modifier = Modifier.background(color = MaterialTheme.colorScheme.primaryContainer))
                        }
                        schema.forEach {
                            column {
                                Text(it)
                            }
                        }
                    },
                    paginationState = paginationState,
                    onPageChanged = {
                        if(content.isNotEmpty()){
                            content.chunked(it.pageSize)[it.pageIndex - 1]
                        } else{
                            emptyList()
                        }
                    }
                ) { data ->
                    row {
                        data.forEach { item ->
                            cell {
                                Text(
                                    text = item,
                                    modifier = Modifier
                                        .widthIn(min = 100.dp, max = 200.dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}