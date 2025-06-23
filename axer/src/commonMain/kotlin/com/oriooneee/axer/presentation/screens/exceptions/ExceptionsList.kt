package com.oriooneee.axer.presentation.screens.exceptions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.oriooneee.axer.domain.exceptions.AxerException
import com.oriooneee.axer.domain.requests.Transaction
import com.oriooneee.axer.presentation.clickableWithoutRipple
import com.oriooneee.axer.presentation.screens.RequestViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

internal class ExceptionsList {
    @Composable
    fun RequestCard(
        isSelected: Boolean,
        exception: AxerException,
        onClick: () -> Unit,
    ) {
        ListItem(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .clickableWithoutRipple {
                    onClick()
                },
            headlineContent = {
                Text(
                    exception.shortName,
                    color = if (exception.isFatal) MaterialTheme.colorScheme.error else Color.Unspecified,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            supportingContent = {
                Text(exception.formatedTime())
            },
        )
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Screen(
        selectedRequestId: Long? = null,
        onClickToException: (AxerException) -> Unit,
        onClearRequests: () -> Unit,
        onClose: (() -> Unit)?
    ) {
        val viewModel: ExceptionsViewModel = koinViewModel {
            parametersOf(null)
        }

        val exceptions by viewModel.exceptions.collectAsState(emptyList())

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Axer exceptions") },
                    actions = {
                        IconButton(
                            onClick = {
                                onClearRequests()
                                viewModel.deleteAll()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Clear Requests"
                            )
                        }
                    },
                    navigationIcon = {
                        if (onClose != null) {
                            IconButton(onClick = onClose) {
                                Icon(
                                    imageVector = Icons.Outlined.Clear,
                                    contentDescription = "Close"
                                )
                            }
                        }
                    }
                )
            }
        ) { contentPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),

                ) {
                if (exceptions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = androidx.compose.ui.Alignment.Center

                    ) {
                        Text("No exceptions found")
                    }
                } else {
                    LazyColumn {
                        items(exceptions) { item ->
                            RequestCard(
                                isSelected = item.id == selectedRequestId,
                                exception = item,
                                onClick = {
                                    onClickToException(item)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}