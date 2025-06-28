package com.oriooneee.axer.presentation.screens.database

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.oriooneee.axer.room.AxerBundledSQLiteDriver
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

class ListTables {
    @Composable
    fun ListTableContent(tables: List<String>, onClickToTable: (String) -> Unit) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            tables.forEach {
                Card(
                    modifier = Modifier
                        .padding(8.dp),
                    onClick = {
                        onClickToTable(it)
                    }
                ) {
                    Text(
                        text = it,
                        modifier = Modifier
                            .padding(16.dp)

                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Screen(
        onClickToTable: (String) -> Unit,
    ) {
        val viewModel: DatabaseInspectionViewModel = koinViewModel{
            parametersOf(null)
        }
        val isInitialized by AxerBundledSQLiteDriver.isInitialized.collectAsState(false)
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Database") },
                )
            }
        ) { contentPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),

                ) {
                if (!isInitialized) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Driver is not connected to database")
                    }
                } else {
                    LaunchedEffect(Unit) {
                        viewModel.loadTables()
                    }
                    val tables by viewModel.tables.collectAsState()
                    if (tables.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No tables found")
                        }
                    } else {
                        ListTableContent(
                            tables = tables,
                            onClickToTable = onClickToTable
                        )
                    }
                }
            }
        }
    }
}