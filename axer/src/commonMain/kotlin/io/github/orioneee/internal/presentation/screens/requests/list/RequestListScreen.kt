package io.github.orioneee.internal.presentation.screens.requests.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.WifiTetheringOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.orioneee.LocalAxerDataProvider
import io.github.orioneee.axer.generated.resources.Res
import io.github.orioneee.axer.generated.resources.har
import io.github.orioneee.axer.generated.resources.no_requests_desc
import io.github.orioneee.axer.generated.resources.requests
import io.github.orioneee.internal.domain.other.DataState
import io.github.orioneee.internal.domain.requests.data.Transaction
import io.github.orioneee.internal.logger.formateAsTime
import io.github.orioneee.internal.presentation.components.AxerLogoDialog
import io.github.orioneee.internal.presentation.components.FilterRow
import io.github.orioneee.internal.presentation.components.LoadingDialog
import io.github.orioneee.internal.presentation.components.ScreenLayout
import io.github.orioneee.internal.presentation.components.ServerRunStatus
import io.github.orioneee.internal.presentation.screens.requests.EmptyScreen
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

internal class RequestListScreen() {

    @Composable
    private fun StatusChip(request: Transaction) {
        val status = request.responseStatus
        val color = when {
            request.isInProgress() -> MaterialTheme.colorScheme.secondary
            status != null && status in 200..299 -> MaterialTheme.colorScheme.primary
            status != null && status in 300..399 -> MaterialTheme.colorScheme.tertiary
            status != null && status >= 400 -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.outline
        }
        AssistChip(
            onClick = { },
            label = {
                Text(status?.toString() ?: if (request.isInProgress()) "…" else "—")
            },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = color.copy(alpha = 0.15f),
                labelColor = color
            ),
            border = null
        )
    }

    @Composable
    fun RequestCard(
        isSelected: Boolean,
        request: Transaction,
        onClick: () -> Unit,
    ) {
        val animatedContainerColor by animateColorAsState(
            targetValue = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
            label = "RequestCardColorAnimation"
        )

        val animatedElevation by animateDpAsState(
            targetValue = if (isSelected) 6.dp else 2.dp,
            label = "CardElevationAnim"
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .clickable { onClick() },
            colors = CardDefaults.cardColors(containerColor = animatedContainerColor),
            elevation = CardDefaults.cardElevation(defaultElevation = animatedElevation)
        ) {
            Column(
                modifier = Modifier.padding(
                    vertical = 8.dp,
                     horizontal = 12.dp,
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StatusChip(request)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = request.method,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (request.isInProgress()) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .width(60.dp)
                                .height(4.dp),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                if(request.path.isNotBlank()){
                    Text(
                        text = request.path,
                        color = if (
                            request.error != null ||
                            (request.responseStatus != null && request.isErrorByStatusCode())
                        ) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(2.dp))
                }

                val infoText = buildString {
                    append("${request.host}  ${request.sendTime.formateAsTime()}  ")
                    request.error?.let { append(it.name) }
                    if (request.isFinished()) append("${request.totalTime}ms")
                }
                val fontWeight = animateIntAsState(if (request.isViewed) 400 else 700)
                Text(
                    text = infoText,
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                    fontWeight = FontWeight(fontWeight.value),
                )
            }
        }
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
        val requests = viewModel.filteredRequests.collectAsStateWithLifecycle(emptyList())
        val methodFilters = viewModel.methodFilters.collectAsStateWithLifecycle(emptyList())
        val typeFilters = viewModel.bodyTypeFilters.collectAsStateWithLifecycle(emptyList())
        val selectedMethods = viewModel.selectedMethods.collectAsStateWithLifecycle(emptyList())
        val selectedBodyTypes = viewModel.selectedBodyType.collectAsStateWithLifecycle(emptyList())
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
                Row {
                    AxerLogoDialog()
                    ServerRunStatus(dataProvider)
                }
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
                stickyHeader {
                    Surface {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
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
                    }
                }
                items(requests.value) { item ->
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