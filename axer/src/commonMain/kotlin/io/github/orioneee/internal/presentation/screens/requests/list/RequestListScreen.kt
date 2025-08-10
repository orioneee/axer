package io.github.orioneee.internal.presentation.screens.requests.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.WifiTetheringOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.orioneee.axer.generated.resources.Res
import io.github.orioneee.axer.generated.resources.har
import io.github.orioneee.axer.generated.resources.no_requests_desc
import io.github.orioneee.axer.generated.resources.requests
import io.github.orioneee.internal.domain.other.DataState
import io.github.orioneee.internal.domain.requests.data.Transaction
import io.github.orioneee.internal.extentions.clickableWithoutRipple
import io.github.orioneee.internal.logger.formateAsTime
import io.github.orioneee.LocalAxerDataProvider
import io.github.orioneee.internal.presentation.components.AxerLogoDialog
import io.github.orioneee.internal.presentation.components.FilterRow
import io.github.orioneee.internal.presentation.components.LoadingDialog
import io.github.orioneee.internal.presentation.components.ScreenLayout
import io.github.orioneee.internal.presentation.screens.requests.EmptyScreen
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

internal class RequestListScreen() {
    @Composable
    fun RequestCard(
        isSelected: Boolean,
        request: Transaction,
        onClick: () -> Unit,
    ) {
        val animatedContainerColor = animateColorAsState(
            targetValue = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
            label = "RequestCardColorAnimation"
        )
        val animatedFontWeight = animateIntAsState(if (request.isViewed) 400 else 700)
        ListItem(
            colors = ListItemDefaults.colors(containerColor = animatedContainerColor.value),
            modifier = Modifier.Companion
                .clip(RoundedCornerShape(16.dp))
                .clickableWithoutRipple {
                    onClick()
                },
            headlineContent = {
                val annotatedString = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Companion.Bold
                        )
                    ) {
                        append(request.method)
                    }
                    append(" ${request.path}")
                }
                Text(
                    annotatedString,
                    color = if (
                        request.error != null ||
                        (request.responseStatus != null && request.isErrorByStatusCode())
                    ) MaterialTheme.colorScheme.error else Color.Companion.Unspecified,
                    maxLines = 3,
                    overflow = TextOverflow.Companion.Ellipsis,
                    fontWeight = FontWeight(animatedFontWeight.value)
                )
            },
            supportingContent = {
                val string = StringBuilder()
                string.append("${request.host} ${request.sendTime.formateAsTime()} ")
                request.error?.let {
                    string.append(it.name)
                }
                if (request.isFinished()) {
                    string.append("${request.totalTime}ms")
                }

                Text(string.toString())
            },
            trailingContent = {
                if (request.isInProgress()) {
                    CircularProgressIndicator(
                        modifier = Modifier.Companion
                            .size(24.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(request.responseStatus?.toString() ?: "")
                }
            }
        )
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Screen(
        selectedRequestId: Long? = null,
        onClickToRequestDetails: (Transaction) -> Unit,
        onClearRequests: () -> Unit,
    ) {
        val dataProvider = LocalAxerDataProvider.current
        val viewModel: RequestListViewModel = koinViewModel {
            parametersOf(dataProvider)
        }
        val state = viewModel.requestsState.collectAsStateWithLifecycle(DataState.Loading())
        val isShowLoadingDialog = viewModel.isShowLoadingDialog.collectAsStateWithLifecycle(false)
        val requests = viewModel.filteredRequests.collectAsState(emptyList())
        val methodFilters = viewModel.methodFilters.collectAsState(emptyList())
        val typeFilters = viewModel.bodyTypeFilters.collectAsState(emptyList())
        val selectedMethods = viewModel.selectedMethods.collectAsState(emptyList())
        val selectedBodyTypes = viewModel.selectedBodyType.collectAsState(emptyList())
        LoadingDialog(
            isShow = isShowLoadingDialog.value,
            onCancel = viewModel::cancelCurrentJob
        )
        ScreenLayout(
            state = state.value,
            isEmpty = { it.isEmpty() },
            topAppBarTitle = stringResource(Res.string.requests),
            actions = {
                AnimatedVisibility(
                    visible = requests.value.any { it.isFinished() },
                    enter = fadeIn() + slideInHorizontally {
                        it
                    },
                    exit = fadeOut() + slideOutHorizontally {
                        it
                    }
                ) {
                    TextButton(
                        onClick = {
                            viewModel.exportAsHar()
                        }
                    ) {
                        Text(stringResource(Res.string.har))
                    }
                }
                IconButton(
                    onClick = {
                        onClearRequests()
                        viewModel.clearAll()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Clear Requests"
                    )
                }
            },
            navigationIcon = {
                AxerLogoDialog()
            },
            emptyContent = {
                EmptyScreen().Screen(
                    image = rememberVectorPainter(Icons.Outlined.WifiTetheringOff),
                    description = stringResource(Res.string.no_requests_desc)
                )
            }
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                item {
                    if (methodFilters.value.isNotEmpty()) {
                        FilterRow(
                            items = methodFilters.value,
                            selectedItems = selectedMethods.value,
                            onItemClicked = { method ->
                                viewModel.toggleMethodFilter(method)
                            },
                            onClear = {
                                viewModel.clearMethodFilters()
                            },
                            getItemString = { it }
                        )
                    }
                }
                item {
                    if (typeFilters.value.isNotEmpty()) {
                        FilterRow(
                            items = typeFilters.value,
                            selectedItems = selectedBodyTypes.value,
                            onItemClicked = { filter ->
                                viewModel.toggleTypeFilter(filter)
                            },
                            onClear = {
                                viewModel.clearTypeFilters()
                            },
                            getItemString = { it.name }
                        )
                    }
                }
                items(requests.value) { item ->
                    Box(
                        modifier = Modifier.Companion
                            .fillMaxWidth()
                            .animateItem(
                                fadeInSpec = tween(300),
                                fadeOutSpec = tween(300),
                                placementSpec = tween(300)
                            )
                    ) {
                        RequestCard(
                            isSelected = item.id == selectedRequestId,
                            request = item,
                            onClick = {
                                onClickToRequestDetails(item)
                            }
                        )
                    }
                }
            }
        }
    }
}