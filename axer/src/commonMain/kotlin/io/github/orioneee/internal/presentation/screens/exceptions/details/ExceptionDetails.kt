package io.github.orioneee.internal.presentation.screens.exceptions.details

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import io.github.aakira.napier.LogLevel
import io.github.orioneee.axer.generated.resources.Res
import io.github.orioneee.axer.generated.resources.courier
import io.github.orioneee.axer.generated.resources.less
import io.github.orioneee.axer.generated.resources.message
import io.github.orioneee.axer.generated.resources.more
import io.github.orioneee.axer.generated.resources.name
import io.github.orioneee.axer.generated.resources.no_exception_found_with_id
import io.github.orioneee.axer.generated.resources.stack_trace
import io.github.orioneee.axer.generated.resources.time
import io.github.orioneee.internal.domain.exceptions.SessionEvent
import io.github.orioneee.internal.domain.requests.ResponseShort
import io.github.orioneee.internal.logger.formateAsDate
import io.github.orioneee.internal.logger.formateAsTime
import io.github.orioneee.LocalAxerDataProvider
import io.github.orioneee.axer.generated.resources.timeline
import io.github.orioneee.internal.presentation.components.MyRatioButton
import io.github.orioneee.internal.presentation.components.MyVerticalLine
import io.github.orioneee.internal.presentation.components.PhantomMyRatioButton
import io.github.orioneee.internal.presentation.components.PlatformHorizontalScrollBar
import io.github.orioneee.internal.presentation.components.ScreenLayout
import io.github.orioneee.internal.presentation.components.buildStringSection
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

internal class ExceptionDetails {

    @Composable
    private fun EventIcon(event: SessionEvent): ImageVector {
        return when (event) {
            is SessionEvent.Exception -> Icons.Default.ErrorOutline
            is SessionEvent.Log -> Icons.Default.Description
            is SessionEvent.Request -> Icons.Default.ArrowUpward
            is SessionEvent.Response -> Icons.Default.ArrowDownward
        }
    }

    @Composable
    private fun EventColor(event: SessionEvent): Color {
        return when (event) {
            is SessionEvent.Exception -> MaterialTheme.colorScheme.error
            is SessionEvent.Log -> if (event.logLine.level == LogLevel.ERROR || event.logLine.level == LogLevel.ASSERT)
                MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface

            is SessionEvent.Request -> MaterialTheme.colorScheme.tertiary
            is SessionEvent.Response -> {
                val isError = event.response is ResponseShort.Error ||
                        (event.response is ResponseShort.Success && event.response.isErrorByStatusCode())
                if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
            }
        }
    }

    @Composable
    fun buildEventText(event: SessionEvent): AnnotatedString {
        return when (event) {
            is SessionEvent.Exception -> buildAnnotatedString {
                withStyle(SpanStyle(color = EventColor(event))) {
                    append("${event.eventTime.formateAsDate()} ${event.exception.error.name} - ${event.exception.error.message}")
                }
            }

            is SessionEvent.Log -> buildAnnotatedString {
                withStyle(SpanStyle(color = EventColor(event))) {
                    append(event.logLine.toString())
                }
            }

            is SessionEvent.Request -> buildAnnotatedString {
                withStyle(SpanStyle(color = EventColor(event))) {
                    append("${event.eventTime.formateAsDate()} ↑ ${event.request.method} - ${event.request.path}")
                }
            }

            is SessionEvent.Response -> {
                val arrow = "↓"
                val baseText = when (event.response) {
                    is ResponseShort.Error -> "${event.response.method} - ${event.response.path} - ${event.response.name}"
                    is ResponseShort.Success -> "${event.response.method} - ${event.response.path} - ${event.response.status}"
                }
                buildAnnotatedString {
                    append("${event.eventTime.formateAsDate()} $arrow ")
                    withStyle(SpanStyle(color = EventColor(event))) {
                        append(baseText)
                    }
                }
            }
        }
    }

    @Composable
    fun DisplayEvents(events: List<SessionEvent>) {
        var isShortView by remember { mutableStateOf(true) }
        val rotating = animateFloatAsState(if (isShortView) 0f else 180f)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .horizontalScroll(scrollState)
                        .padding(8.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.timeline),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    events.forEachIndexed { index, event ->
                        val isVisible = remember(isShortView, event, index) {
                            !(isShortView && index > 5)
                        }
                        AnimatedVisibility(
                            visible = isVisible
                        ) {
                            Row(
                                modifier = Modifier.height(IntrinsicSize.Min),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxHeight(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val isFirst = index == 0
                                    val isLast = index == events.size - 1
                                    MyVerticalLine(
                                        onClick = {},
                                        modifier = Modifier.fillMaxHeight(),
                                        isFirst = isFirst,
                                        isLast = isLast,
                                    )
                                    PhantomMyRatioButton(
                                        selected = isFirst,
                                        radioColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    MyRatioButton(selected = isFirst, onClick = {})
                                }
                                Box(
                                    modifier = Modifier.fillMaxHeight(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = buildEventText(event),
                                        modifier = Modifier
                                            .padding(8.dp),
                                        fontFamily = FontFamily(
                                            Font(Res.font.courier)
                                        ),
                                        maxLines = 10,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    PlatformHorizontalScrollBar(scrollState)
                }
                if (events.size > 5) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        TextButton(
                            onClick = {
                                isShortView = !isShortView
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                AnimatedContent(isShortView) {
                                    if (it) {
                                        Text(text = stringResource(Res.string.more))
                                    } else {
                                        Text(text = stringResource(Res.string.less))
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Outlined.KeyboardArrowDown,
                                    contentDescription = "Toggle View",
                                    modifier = Modifier.rotate(rotating.value)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Screen(
        navController: NavHostController,
        exceptionID: Long,
    ) {
        val provider = LocalAxerDataProvider.current
        val viewModel: ExceptionDetailsViewModel = koinViewModel {
            parametersOf(provider, exceptionID)
        }
        val data by viewModel.data.collectAsState(null)
        val exception = data?.exception
        val isLoading by viewModel.isLoading.collectAsState(false)

        val title = if (exception != null) {
            "${exception.error.name} - ${exception.time.formateAsTime()}"
        } else ""

        ScreenLayout(
            isLoading = isLoading,
            isEmpty = false,
            topAppBarTitle = title,
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back")
                }
            },
            emptyContent = {
                Text(stringResource(Res.string.no_exception_found_with_id, exception?.id ?: ""))
            }
        ) {
            if (exception == null) return@ScreenLayout

            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Exception metadata card
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                    ) {
                        SelectionContainer {
                            Text(
                                buildStringSection(
                                    stringResource(Res.string.name),
                                    exception.error.name
                                )
                            )
                        }
                        SelectionContainer {
                            Text(
                                buildStringSection(
                                    stringResource(Res.string.message),
                                    exception.error.message
                                )
                            )
                        }
                        SelectionContainer {
                            Text(
                                buildStringSection(
                                    stringResource(Res.string.time),
                                    exception.time.formateAsDate()
                                )
                            )
                        }
                    }
                }

                // Timeline
                if (data?.events?.isNotEmpty() == true && data!!.events.size > 1) {
                    DisplayEvents(data!!.events)
                }

                // Stack trace with collapse
                if (exception.error.stackTrace.isNotBlank()) {
                    var expanded by remember { mutableStateOf(false) }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expanded = !expanded }
                                    .padding(16.dp)
                                ,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(Res.string.stack_trace),
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Outlined.KeyboardArrowDown,
                                    contentDescription = null,
                                    modifier = Modifier.rotate(if (expanded) 180f else 0f)
                                )
                            }

                            AnimatedVisibility(expanded) {
                                Box(
                                    modifier = Modifier
                                        .horizontalScroll(rememberScrollState())
                                        .padding(8.dp)
                                ) {
                                    SelectionContainer {
                                        Text(
                                            text = exception.error.stackTrace,
                                            fontFamily = FontFamily.Monospace,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
