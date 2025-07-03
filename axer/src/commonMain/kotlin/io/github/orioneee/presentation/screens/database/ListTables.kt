package io.github.orioneee.presentation.screens.database

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.RawOn
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import io.github.orioneee.domain.database.Table
import io.github.orioneee.extentions.formate
import io.github.orioneee.presentation.components.AxerLogo
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
            columns = GridCells.Adaptive(minSize = 250.dp),
        ) {
            items(tables) {
                var rowCountTarget = remember { mutableStateOf(it.rowCount) }

                val animatedRowCount = animateIntAsState(rowCountTarget.value)

                LaunchedEffect(it.rowCount) {
                    rowCountTarget.value = it.rowCount
                }
                ListItem(
                    headlineContent = {
                        Text(
                            text = it.name,
                            maxLines = 1
                        )
                    },
                    supportingContent = {
                        Text(
                            text = "Rows: ${animatedRowCount.value.formate()}, Columns: ${it.columnCount}",
                            maxLines = 1
                        )
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onClickToTable(it) },
                )
            }
            item(
                span = { GridItemSpan(1) }
            ) {
                Spacer(Modifier.height(70.dp))

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
                    },
                    navigationIcon = {
                        AxerLogo()
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        navController.navigate(Routes.ALL_QUERIES.route)
                    }
                ) {
                    Text("All Queries", Modifier.padding(4.dp))
                }
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