package io.github.orioneee.internal.presentation.screens.database.tableList

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import io.github.orioneee.axer.generated.resources.Res
import io.github.orioneee.axer.generated.resources.all_queries
import io.github.orioneee.axer.generated.resources.database
import io.github.orioneee.axer.generated.resources.ic_database_remove
import io.github.orioneee.axer.generated.resources.no_databases_desc
import io.github.orioneee.axer.generated.resources.rows_columns
import io.github.orioneee.internal.domain.database.DatabaseWrapped
import io.github.orioneee.internal.domain.database.Table
import io.github.orioneee.internal.domain.other.DataState
import io.github.orioneee.internal.extentions.formate
import io.github.orioneee.LocalAxerDataProvider
import io.github.orioneee.internal.presentation.components.AxerLogoDialog
import io.github.orioneee.internal.presentation.components.BodySection
import io.github.orioneee.internal.presentation.components.ScreenLayout
import io.github.orioneee.internal.presentation.navigation.Routes
import io.github.orioneee.internal.presentation.screens.requests.EmptyScreen
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

internal class ListTables {

    @Composable
    fun ListTableContent(
        table: Table,
        onClickToTable: (Table) -> Unit
    ) {
        val rowCountTarget = remember(table) { mutableStateOf(table.rowCount) }

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
        val provider = LocalAxerDataProvider.current
        val viewModel: ListDatabaseViewModel = koinViewModel {
            parametersOf(provider)
        }
        val state by viewModel.databases.collectAsStateWithLifecycle(DataState.Loading())
        ScreenLayout(
            state = state,
            isEmpty = { it.isEmpty() },
            topAppBarTitle = stringResource(Res.string.database),
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
            navigationIcon = {
                AxerLogoDialog()
            },
            emptyContent = {
                EmptyScreen().Screen(
                    image = painterResource(Res.drawable.ic_database_remove),
                    description = stringResource(Res.string.no_databases_desc)
                )
            }
        ) {
            ListDatabase(
                databases = it,
                onClickToTable = { table, file ->
                    navController.navigate(Routes.TABLE_DETAILS.route + "/$file/${table.name}")
                },
                navController = navController,
            )
        }
    }
}