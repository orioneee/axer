package io.github.orioneee.presentation.screens.database.tableList

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import io.github.orioneee.axer.generated.resources.Res
import io.github.orioneee.axer.generated.resources.all_queries
import io.github.orioneee.axer.generated.resources.database
import io.github.orioneee.axer.generated.resources.driver_not_connected
import io.github.orioneee.axer.generated.resources.nothing_found
import io.github.orioneee.axer.generated.resources.rows_columns
import io.github.orioneee.domain.database.DatabaseWrapped
import io.github.orioneee.domain.database.Table
import io.github.orioneee.extentions.formate
import io.github.orioneee.presentation.components.AxerLogo
import io.github.orioneee.presentation.components.BodySection
import io.github.orioneee.presentation.navigation.Routes
import io.github.orioneee.room.AxerBundledSQLiteDriver
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

internal class ListTables {

    @Composable
    fun ListTableContent(
        table: Table,
        onClickToTable: (Table) -> Unit
    ) {
        var rowCountTarget = remember { mutableStateOf(table.rowCount) }

        val animatedRowCount = animateIntAsState(rowCountTarget.value)

        LaunchedEffect(table.rowCount) {
            rowCountTarget.value = table.rowCount
        }
        ListItem(
            headlineContent = {
                Text(
                    text = table.name,
                    maxLines = 1
                )
            },
            supportingContent = {
                Text(
                    text = stringResource(
                        Res.string.rows_columns,
                        animatedRowCount.value.formate(),
                        table.columnCount
                    ),
                    maxLines = 1
                )
            },
            modifier = Modifier.Companion
                .clip(RoundedCornerShape(8.dp))
                .clickable { onClickToTable(table) },
        )
    }


    @Composable
    fun ListDatabase(
        databases: List<DatabaseWrapped>,
        onClickToTable: (Table, String) -> Unit,
        navController: NavHostController,
    ) {
        LazyVerticalStaggeredGrid(
            modifier = Modifier.Companion
                .fillMaxSize()
                .padding(horizontal = 4.dp),
            columns = StaggeredGridCells.Adaptive(minSize = 300.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            items(databases) { database ->
                BodySection(
                    database.name,
                    separator = "",
                    isExpandable = false,
                    onClick = {
                        navController.navigate(Routes.RAW_QUERY.route + "/${database.name}")
                    },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(8.dp)
                    ) {
                        database.tables.forEach { table ->
                            ListTableContent(
                                table = table,
                                onClickToTable = { onClickToTable(table, database.name) }
                            )
                        }
                    }
                }
            }
            item(
                span = StaggeredGridItemSpan.FullLine
            ) {
                Spacer(Modifier.Companion.height(70.dp))
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Screen(
        navController: NavHostController,
    ) {
        val viewModel: ListDatabaseViewModel = koinViewModel()
        val isInitialized = AxerBundledSQLiteDriver.isInitialized.collectAsState(false)
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            stringResource(Res.string.database),
                            style = MaterialTheme.typography.titleSmall
                        )
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
                    Text(
                        stringResource(Res.string.all_queries),
                        Modifier.Companion.padding(vertical = 4.dp, horizontal = 8.dp)
                    )
                }
            },
        ) { contentPadding ->
            Box(
                modifier = Modifier.Companion
                    .fillMaxSize()
                    .padding(contentPadding),

                ) {
                val databases = viewModel.databases.collectAsState()
                if (databases.value.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(Res.string.nothing_found))
                    }
                } else {
                    ListDatabase(
                        databases = databases.value,
                        onClickToTable = { table, file ->
                            navController.navigate(Routes.TABLE_DETAILS.route + "/$file/${table.name}")
                        },
                        navController = navController,
                    )
                }
            }
        }
    }
}