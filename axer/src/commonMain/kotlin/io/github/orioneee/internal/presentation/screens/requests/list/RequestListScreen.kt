package io.github.orioneee.internal.presentation.screens.requests.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontFamily
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
import io.github.orioneee.internal.presentation.components.LocalAxerColors
import io.github.orioneee.internal.presentation.components.methodColor
import io.github.orioneee.internal.presentation.components.statusColor
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
    private fun MethodBadge(method: String, color: Color) {
        Surface(
            color = color.copy(alpha = 0.12f),
            shape = RoundedCornerShape(6.dp),
        ) {
            Text(
                text = method,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                ),
                color = color,
            )
        }
    }

    @Composable
    fun RequestCard(
        isSelected: Boolean,
        request: Transaction,
        onClick: () -> Unit,
    ) {
        val axerColors = LocalAxerColors.current
        val statusColor = axerColors.statusColor(request.responseStatus)
        val methodColor = axerColors.methodColor(request.method)

        val animatedBorderColor by animateColorAsState(
            targetValue = when {
                isSelected -> axerColors.cardBorderSelected
                request.isInProgress() -> axerColors.accent.copy(alpha = 0.5f)
                else -> axerColors.cardBorder
            },
            label = "borderColor"
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 3.dp)
                .clip(RoundedCornerShape(14.dp))
                .clickable { onClick() },
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected)
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                else
                    MaterialTheme.colorScheme.surfaceContainerLow
            ),
            border = BorderStroke(1.dp, animatedBorderColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(40.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            if (request.isInProgress()) axerColors.accent.copy(alpha = 0.5f)
                            else statusColor.copy(alpha = 0.7f)
                        )
                )
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MethodBadge(request.method, methodColor)
                        if (request.responseStatus != null) {
                            Text(
                                text = request.responseStatus.toString(),
                                color = statusColor,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        } else if (request.isInProgress()) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .width(40.dp)
                                    .height(3.dp),
                                color = axerColors.accent,
                                trackColor = axerColors.accent.copy(alpha = 0.15f)
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        if (request.isFinished()) {
                            Text(
                                text = "${request.totalTime}ms",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (request.path.isNotBlank()) {
                        Text(
                            text = request.path,
                            color = if (
                                request.error != null ||
                                (request.responseStatus != null && request.isErrorByStatusCode())
                            ) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Monospace
                            ),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val fontWeight = animateIntAsState(if (request.isViewed) 400 else 700)
                        Text(
                            text = request.host,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight(fontWeight.value),
                            maxLines = 1,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Text(
                            text = request.sendTime.formateAsTime(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        request.error?.let {
                            Text(
                                text = it.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                maxLines = 1
                            )
                        }
                    }
                }
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