package com.oriooneee.ktorin.presentation.screens.requestList

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.oriooneee.ktorin.presentation.clickableWithoutRipple
import com.oriooneee.ktorin.presentation.screens.RequestViewModel
import com.oriooneee.ktorin.room.entities.Transaction
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

class RequestListScreen() {
    @Composable
    fun RequestCard(
        isSelected: Boolean,
        request: Transaction,
        onClick: () -> Unit
    ) {
        val animatedContainerColor by animateColorAsState(
            targetValue = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
            label = "RequestCardColorAnimation"
        )
        ListItem(
            colors = ListItemDefaults.colors(containerColor = animatedContainerColor),
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .clickableWithoutRipple {
                    onClick()
                }
            ,
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
                Text(annotatedString)
            },
            supportingContent = {
                val text = "${request.host} ${request.formatedSendTime()} " + if(request.isFinished()) "${request.totalTime}ms" else ""
                Text(text)
            },
            leadingContent = {
                if(request.isInProgress()){
                    CircularProgressIndicator()
                } else{
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
        onClearRequests: () -> Unit
    ) {
        val viewModel: RequestViewModel = koinViewModel{
            parametersOf(null)
        }
        val requests by viewModel.requests.collectAsState(emptyList())
        Scaffold (
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
                    }
                )
            }
        ){ contentPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),

                ) {
                LazyColumn {
                    items(requests) { item ->
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