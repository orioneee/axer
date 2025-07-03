package io.github.orioneee.presentation.screens.logView

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import io.github.aakira.napier.LogLevel
import io.github.orioneee.logger.formateAsDate
import io.github.orioneee.presentation.components.FilterRow
import io.github.orioneee.presentation.components.PlatformScrollBar
import io.github.orioneee.presentation.components.warning
import org.koin.compose.viewmodel.koinViewModel

class LogViewScreen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Screen(
        navController: NavHostController,
    ) {
        val viewModel: LogViewViewModel = koinViewModel()


        val logs = viewModel.logs.collectAsState(listOf())
        val filtredLogs = viewModel.filtredLogs.collectAsState(listOf())

        val tags = viewModel.tags.collectAsState(listOf())
        val levels = viewModel.levels.collectAsState(listOf())
        val selectedTags = viewModel.selectedTags.collectAsState(listOf())
        val selectedLevels = viewModel.selectedLevels.collectAsState(listOf())
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Logs") },
                    actions = {
                        IconButton(
                            onClick = {
                                viewModel.clear()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Clear Queries"
                            )
                        }
                    }
                )
            }
        ) { contentPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
            ) {
                if (logs.value.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Text("No logs found")
                    }
                } else {
                    Column {
                        FilterRow(
                            items = tags.value,
                            selectedItems = selectedTags.value,
                            onItemClicked = { tag ->
                                viewModel.toggleTag(tag)
                            },
                            onClear = {
                                viewModel.clearTags()
                            },
                            getItemString = {
                                it
                            }
                        )
                        FilterRow(
                            items = levels.value,
                            selectedItems = selectedLevels.value,
                            onItemClicked = { level ->
                                viewModel.toggleLevel(level)
                            },
                            onClear = {
                                viewModel.clearLevels()
                            },
                            getItemString = {
                                it.name
                            }
                        )
                        val listState = rememberLazyListState()
                        SelectionContainer {
                            Box {
                                Column {
                                    LazyColumn(
                                        state = listState,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 4.dp)
                                            .horizontalScroll(rememberScrollState())
                                    ) {
                                        items(
                                            items = logs.value,
                                            key = {
                                                it.id
                                            }
                                        ) {
                                            AnimatedVisibility(
                                                visible = filtredLogs.value.contains(it),
                                                enter = fadeIn() + expandVertically(),
                                                exit = fadeOut() + shrinkVertically(),
                                            ) {
                                                Row {
                                                    val color =
                                                        if (it.level == LogLevel.ERROR || it.level == LogLevel.ASSERT) {
                                                            MaterialTheme.colorScheme.error
                                                        } else if (it.level == LogLevel.WARNING) {
                                                            MaterialTheme.colorScheme.warning
                                                        } else {
                                                            MaterialTheme.colorScheme.onSurface
                                                        }
                                                    Text(
                                                        "${it.time.formateAsDate()} - ${it.level.name} - ${it.tag} - ",
                                                        color = color
                                                    )
                                                    Text(it.message, color = color)
                                                }
                                            }
                                        }
                                    }
                                }
                                PlatformScrollBar(listState)
                            }
                        }
                    }
                }
            }
        }
    }
}