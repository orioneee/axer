package io.github.orioneee.internal.presentation.screens.database.rawQuery

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.alexzhirkevich.compottie.LottieComposition
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.animateLottieCompositionAsState
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import io.github.orioneee.LocalAxerDataProvider
import io.github.orioneee.axer.generated.resources.Res
import io.github.orioneee.axer.generated.resources.oops
import io.github.orioneee.axer.generated.resources.try_again
import io.github.orioneee.internal.domain.database.EditableRowItem
import io.github.orioneee.internal.domain.database.QueryResponse
import io.github.orioneee.internal.extentions.sortBySortingItemAndChunck
import io.github.orioneee.internal.presentation.components.ContentCell
import io.github.orioneee.internal.presentation.components.HeaderCell
import io.github.orioneee.internal.presentation.components.PaginationUI
import io.github.orioneee.internal.presentation.components.ViewTable
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.time.Duration.Companion.milliseconds

internal class RawQueryScreen {
    @Composable
    fun LoadingAnimation(
    ) {
        LinearProgressIndicator(
            modifier = Modifier
                .width(120.dp)
                .height(4.dp),
            color = MaterialTheme.colorScheme.primary
        )
    }

    @Composable
    fun DisplayError(
        error: Throwable,
        composition: LottieComposition?,
        onRetry: () -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Lottie animation
            val progress = animateLottieCompositionAsState(composition = composition)
            val painter = rememberLottiePainter(
                composition = composition,
                progress = { progress.value }
            )
            Image(
                painter = painter,
                contentDescription = "Error",
                modifier = Modifier
                    .size(180.dp)
                    .padding(bottom = 24.dp)
            )

            // Headline
            Text(
                text = stringResource(Res.string.oops),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.error
            )

            Spacer(Modifier.height(8.dp))

            // Detailed message
            Text(
                text = error.message?.takeIf { it.isNotBlank() } ?: "Unknown error occurred.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(24.dp))

            Button(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.height(48.dp),
                onClick = onRetry
            ) {
                Text(stringResource(Res.string.try_again))
            }
        }
    }


    @Composable
    fun DisplayData(
        viewModel: RawQueryViewModel,
        queryResponse: QueryResponse,
    ) {
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
        var selectedCellForPreview: EditableRowItem? by remember {
            mutableStateOf(null)
        }
        var isShowingPreview by remember {
            mutableStateOf(false)
        }
        val currentItems = pages.getOrNull(currentPage) ?: emptyList()
        val sortColumn by viewModel.sortColumn.collectAsState()

        PaginationUI(
            totalItems = queryResponse.rows.size,
            page = currentPage,
            onSetPage = { newPage ->
                currentPage = newPage
            },
            currentItemsSize = currentItems.size,
        )
        AnimatedVisibility(
            isShowingPreview,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            DisposableEffect(Unit) {
                onDispose {
                    selectedCellForPreview = null
                }
            }
            SelectionContainer {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {

                    Text(
                        selectedCellForPreview?.editedValue?.value ?: "",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
        ViewTable(
            headers = schema,
            rows = currentItems,
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
                val isSelected = selectedCellForPreview?.let {
                    it.schemaItem == schema && it.selectedColumnIndex == columnIndex && it.item == line && it.editedValue == cellData && isShowingPreview
                } ?: false
                ContentCell(
                    text = cellData?.value ?: "NULL",
                    alignment = Alignment.CenterStart,
                    backgroundColor = when {
                        isSelected -> MaterialTheme.colorScheme.tertiaryContainer
                        isPrimary -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.surface
                    },
                    isClickable = true,
                    onClick = {
                        if (isSelected) {
                            isShowingPreview = false
                        } else {
                            selectedCellForPreview = EditableRowItem(
                                schemaItem = schema,
                                selectedColumnIndex = columnIndex,
                                item = line,
                                editedValue = cellData
                            )
                            isShowingPreview = true
                        }
                    },
                )
            },
        )
    }

    @OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
    @Composable
    fun Screen(
        name: String,
        onBack: () -> Unit,
    ) {
        val provider = LocalAxerDataProvider.current
        val viewModel: RawQueryViewModel = koinViewModel {
            parametersOf(provider, name)
        }
        val isLoading by viewModel.loading
            .debounce {
                if (it) 100.milliseconds else 700.milliseconds
            }.collectAsState(false)

        val currentQuery by viewModel.currentQuery.collectAsState("")

        val composition by rememberLottieComposition {
            val json = Res.readBytes("files/error.json")
                .decodeToString()
            LottieCompositionSpec.JsonString(json)
        }
        val resp by viewModel.queryResponse.collectAsState()


        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                CenterAlignedTopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleSmall
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                )
            }) { contentPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.Companion.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top)
            ) {
                stickyHeader {
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
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Save Changes",
                                    modifier = Modifier.rotate(180f)
                                )
                            }
                        },
                        modifier = Modifier
                            .onPreviewKeyEvent { keyEvent ->
                                if (keyEvent.type == KeyEventType.KeyDown) {
                                    if (keyEvent.key == Key.Enter && keyEvent.isCtrlPressed.not() && keyEvent.isShiftPressed.not() && keyEvent.isAltPressed.not() && keyEvent.isMetaPressed.not() && !isLoading) {
                                        viewModel.executeQuery()
                                        return@onPreviewKeyEvent true // consume the event
                                    }
                                }
                                return@onPreviewKeyEvent false // let TextField handle all other keys
                            }

                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                            .padding(vertical = 16.dp),
                        value = currentQuery,
                        onValueChange = {
                            viewModel.setQuery(it)
                        },
                    )
                }
                if (isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            LoadingAnimation()
                        }
                    }
                } else {
                    resp.onSuccess {
                        item {
                            DisplayData(
                                viewModel = viewModel,
                                queryResponse = it
                            )
                        }
                    }.onFailure {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                DisplayError(
                                    error = it,
                                    composition = composition,
                                    onRetry = {
                                        viewModel.executeQuery()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}