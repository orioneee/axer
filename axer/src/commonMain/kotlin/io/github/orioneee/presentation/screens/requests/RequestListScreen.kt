package io.github.orioneee.presentation.screens.requests

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import io.github.orioneee.domain.requests.Transaction
import io.github.orioneee.presentation.clickableWithoutRipple
import io.github.orioneee.formateAsTime
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

internal class RequestListScreen() {
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
        val animatedFontWeight by animateIntAsState(if (request.isViewed) 400 else 700)
        ListItem(
            colors = ListItemDefaults.colors(containerColor = animatedContainerColor),
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .clickableWithoutRipple {
                    onClick()
                },
            headlineContent = {
                val annotatedString = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Bold
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
                        (request.responseStatus != null && !request.responseStatus.toString()
                            .startsWith("2"))
                    ) MaterialTheme.colorScheme.error else Color.Unspecified,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight(animatedFontWeight)
                )
            },
            supportingContent = {
                val text =
                    "${request.host} ${request.sendTime.formateAsTime()} " + if (request.isFinished()) "${request.totalTime}ms" else ""
                Text(text)
            },
            trailingContent = {
                if (request.isInProgress()) {
                    CircularProgressIndicator()
                } else {
                    Text(request.responseStatus?.toString() ?: "")
                }
            }
        )
    }

    @Composable
    fun <T> FilterRow(
        items: List<T>,
        selectedItems: List<T>,
        onItemClicked: (T) -> Unit,
        onClear: () -> Unit,
        getItemString: (T) -> String,
        withClearButton: Boolean = true
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (withClearButton) {
                item {
                    IconButton(
                        onClick = onClear,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Clear,
                            contentDescription = "Clear Filters"
                        )
                    }
                }
            } else {
                item {
                    Spacer(Modifier.width(8.dp))
                }
            }
            items(items) {
                val isSelected = selectedItems.contains(it)
                val itemString = getItemString(it)
                InputChip(
                    label = {
                        Text(itemString)
                    },
                    selected = isSelected,
                    onClick = {
                        onItemClicked(it)
                    },
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
        val viewModel: RequestViewModel = koinViewModel {
            parametersOf(null)
        }
        val requests by viewModel.filteredRequests.collectAsState(emptyList())
        val methodFilters = viewModel.methodFilters.collectAsState(emptyList())
        val imageFilters = viewModel.imageFilters.collectAsState(emptyList())
        val selectedMethods by viewModel.selectedMethods.collectAsState(emptyList())
        val selectedImageFilter by viewModel.selectedImageFilter.collectAsState(emptyList())
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Requests") },
                    actions = {
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
                )
            }
        ) { contentPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),

                ) {
                if (requests.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center

                    ) {
                        Text("No requests found")
                    }
                } else {
                    LazyColumn {
                        item {
                            if (methodFilters.value.isNotEmpty()) {
                                FilterRow(
                                    items = methodFilters.value,
                                    selectedItems = selectedMethods,
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
                            if (imageFilters.value.isNotEmpty()) {
                                FilterRow(
                                    items = imageFilters.value,
                                    selectedItems = selectedImageFilter,
                                    onItemClicked = { filter ->
                                        viewModel.toggleImageFilter(filter)
                                    },
                                    onClear = {
                                        viewModel.clearImageFilters()
                                    },
                                    getItemString = { it }
                                )
                            }
                        }
                        items(requests) { item ->
                            Box(
                                modifier = Modifier
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