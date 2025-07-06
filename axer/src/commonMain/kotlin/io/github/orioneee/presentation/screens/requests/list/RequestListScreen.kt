package io.github.orioneee.presentation.screens.requests.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import io.github.orioneee.axer.generated.resources.Res
import io.github.orioneee.axer.generated.resources.har
import io.github.orioneee.axer.generated.resources.nothing_found
import io.github.orioneee.axer.generated.resources.requests
import io.github.orioneee.domain.requests.Transaction
import io.github.orioneee.extentions.clickableWithoutRipple
import io.github.orioneee.logger.formateAsTime
import io.github.orioneee.presentation.components.AxerLogo
import io.github.orioneee.presentation.components.FilterRow
import io.github.orioneee.unitls.exportAsHar
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

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
                if (request.error != null) {
                    string.append(request.error.name)
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
        val viewModel: RequestListViewModel = koinViewModel()
        val requests = viewModel.filteredRequests.collectAsState(emptyList())
        val methodFilters = viewModel.methodFilters.collectAsState(emptyList())
        val typeFilters = viewModel.bodyTypeFilters.collectAsState(emptyList())
        val selectedMethods = viewModel.selectedMethods.collectAsState(emptyList())
        val selectedBodyTypes = viewModel.selectedBodyType.collectAsState(emptyList())
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            stringResource(Res.string.requests),
                            style = MaterialTheme.typography.titleSmall
                        )
                    },
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
                                    requests.value.exportAsHar()
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
                        AxerLogo()
                    }
                )
            }
        ) { contentPadding ->
            Box(
                modifier = Modifier.Companion
                    .fillMaxSize()
                    .padding(contentPadding),

                ) {
                if (requests.value.isEmpty()) {
                    Box(
                        modifier = Modifier.Companion
                            .fillMaxSize(),
                        contentAlignment = Alignment.Companion.Center

                    ) {
                        Text(stringResource(Res.string.nothing_found))
                    }
                } else {
                    LazyColumn {
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
    }
}