package io.github.orioneee.presentation.screens.database

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.RawOn
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import io.github.orioneee.domain.database.Table
import io.github.orioneee.presentation.navigation.Routes
import io.github.orioneee.room.AxerBundledSQLiteDriver
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

internal class ListTables {
    @Composable
    fun ListTableContent(tables: List<Table>, onClickToTable: (Table) -> Unit) {
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            columns = GridCells.Adaptive(minSize = 200.dp)
        ) {
            items(tables) {
                Card(
                    modifier = Modifier
                        .padding(4.dp)
                        .fillMaxWidth(),
                    onClick = { onClickToTable(it) }
                ) {
                    Text(
                        text = "${it.name} (${it.rowCount} rows)",
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(8.dp)
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Screen(
        onClickToTable: (Table) -> Unit,
        navController: NavHostController,
    ) {
        val viewModel: DatabaseInspectionViewModel = koinViewModel {
            parametersOf(null)
        }
        val isInitialized by AxerBundledSQLiteDriver.isInitialized.collectAsState(false)
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Database") },
                    actions = {
                        IconButton(
                            enabled = isInitialized,
                            onClick = {
                                navController.navigate(Routes.RAW_QUERY.route)
                            }
                        ) {
                            Icon(
                                Icons.Outlined.RawOn,
                                null,
                            )
                        }
                    }
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