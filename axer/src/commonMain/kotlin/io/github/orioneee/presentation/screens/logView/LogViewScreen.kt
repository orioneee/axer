package io.github.orioneee.presentation.screens.logView

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import io.github.aakira.napier.LogLevel
import io.github.orioneee.axer.generated.resources.Res
import io.github.orioneee.axer.generated.resources.courier
import io.github.orioneee.axer.generated.resources.export
import io.github.orioneee.axer.generated.resources.ic_export_logs
import io.github.orioneee.axer.generated.resources.logs
import io.github.orioneee.axer.generated.resources.nothing_found
import io.github.orioneee.domain.logs.LogLine
import io.github.orioneee.presentation.LocalAxerDataProvider
import io.github.orioneee.presentation.components.AxerLogoDialog
import io.github.orioneee.presentation.components.FilterRow
import io.github.orioneee.presentation.components.MyRatioButton
import io.github.orioneee.presentation.components.MyVerticalLine
import io.github.orioneee.presentation.components.PlatformScrollBar
import io.github.orioneee.presentation.components.warning
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

internal class LogViewScreen {

    @Composable
    fun DisplayLogline(
        log: LogLine,
    ) {
        val color =
            if (log.level == LogLevel.ERROR || log.level == LogLevel.ASSERT) {
                MaterialTheme.colorScheme.error
            } else if (log.level == LogLevel.WARNING) {
                MaterialTheme.colorScheme.warning
            } else {
                MaterialTheme.colorScheme.onSurface
            }

        Text(
            text = log.toString(),
            color = color,
            modifier = Modifier
                .padding(start = 8.dp),
            fontFamily = FontFamily(
                Font(Res.font.courier)
            )
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Screen(
        navController: NavHostController,
    ) {
        val dataProvider = LocalAxerDataProvider.current
        val viewModel: LogViewViewModel = koinViewModel {
            parametersOf(dataProvider)
        }


        val logs = viewModel.logs.collectAsState(listOf())
        val filtredLogs = viewModel.filtredLogs.collectAsState(listOf())
        val selectedForExport = viewModel.selectedForExport.collectAsState(listOf())

        val tags = viewModel.tags.collectAsState(listOf())
        val levels = viewModel.levels.collectAsState(listOf())
        val selectedTags = viewModel.selectedTags.collectAsState(listOf())
        val selectedLevels = viewModel.selectedLevels.collectAsState(listOf())

        val isExporting = viewModel.isExporting.collectAsState(false)
        val firstExportPointId = viewModel.firstExportPointId.collectAsState(null)
        val lastExportPointId = viewModel.lastExportPointId.collectAsState(null)
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                CenterAlignedTopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = {
                        Text(
                            stringResource(Res.string.logs),
                            style = MaterialTheme.typography.titleSmall
                        )
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                viewModel.onClickExport()
                            }
                        ) {
                            AnimatedContent(
                                targetState = isExporting.value
                            ) {
                                if (it) {
                                    Icon(
                                        Icons.Outlined.Close,
                                        contentDescription = "Cancel Export",
                                    )
                                } else {
                                    Icon(
                                        painterResource(Res.drawable.ic_export_logs),
                                        contentDescription = "Export",
                                    )
                                }
                            }
                        }
                        AnimatedVisibility(
                            visible = isExporting.value &&
                                    (firstExportPointId.value != null && lastExportPointId.value != null)
                                    && selectedForExport.value.isNotEmpty(),
                        ) {
                            TextButton(
                                onClick = {
                                    viewModel.onExport()
                                }
                            ) {
                                Text(stringResource(Res.string.export))
                            }
                        }
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
                    },
                    navigationIcon = {
                        AxerLogoDialog()
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
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(Res.string.nothing_found))
                    }
                } else {
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
                                    item {
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
                                            },
                                            scrolable = false
                                        )
                                    }
                                    item {
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
                                            },
                                            scrolable = false
                                        )
                                    }
                                    items(
                                        items = logs.value,
                                        key = {
                                            it.id
                                        }
                                    ) { line ->
                                        AnimatedVisibility(
                                            visible = filtredLogs.value.contains(line),
                                            enter = fadeIn() + expandVertically(),
                                            exit = fadeOut() + shrinkVertically(),
                                        ) {
                                            val showButtonAsLine =
                                                line in selectedForExport.value &&
                                                        firstExportPointId.value != line.id &&
                                                        lastExportPointId.value != line.id
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .padding(horizontal = 8.dp)
                                                    .height(IntrinsicSize.Max)
                                            ) {
                                                AnimatedVisibility(visible = isExporting.value) {
                                                    AnimatedContent(targetState = showButtonAsLine) { showLine ->
                                                        if (!showLine) {
                                                            MyRatioButton(
                                                                selected = firstExportPointId.value == line.id ||
                                                                        lastExportPointId.value == line.id,
                                                                onClick = {
                                                                    viewModel.onSelectPoint(
                                                                        line.id
                                                                    )
                                                                }
                                                            )
                                                        } else {
                                                            MyVerticalLine(
                                                                onClick = {
                                                                    viewModel.onSelectPoint(
                                                                        line.id
                                                                    )
                                                                },
                                                                modifier = Modifier.fillMaxHeight()
                                                            )
                                                        }
                                                    }
                                                }

                                                DisplayLogline(line)
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