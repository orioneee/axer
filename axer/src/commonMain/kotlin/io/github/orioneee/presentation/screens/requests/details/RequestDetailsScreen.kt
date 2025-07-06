package io.github.orioneee.presentation.screens.requests.details

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import io.github.orioneee.axer.generated.resources.Res
import io.github.orioneee.axer.generated.resources.developer_mark_this_as_important
import io.github.orioneee.axer.generated.resources.duration
import io.github.orioneee.axer.generated.resources.error
import io.github.orioneee.axer.generated.resources.headers
import io.github.orioneee.axer.generated.resources.important
import io.github.orioneee.axer.generated.resources.json
import io.github.orioneee.axer.generated.resources.method
import io.github.orioneee.axer.generated.resources.no_request_found_with_id
import io.github.orioneee.axer.generated.resources.non_formated
import io.github.orioneee.axer.generated.resources.request_failed
import io.github.orioneee.axer.generated.resources.request_size
import io.github.orioneee.axer.generated.resources.request_tab
import io.github.orioneee.axer.generated.resources.response_size
import io.github.orioneee.axer.generated.resources.response_tab
import io.github.orioneee.axer.generated.resources.status
import io.github.orioneee.axer.generated.resources.unknown
import io.github.orioneee.axer.generated.resources.url
import io.github.orioneee.axer.generated.resources.what_is_important
import io.github.orioneee.domain.requests.Transaction
import io.github.orioneee.domain.requests.formatters.BodyType
import io.github.orioneee.presentation.components.BodySection
import io.github.orioneee.presentation.components.MultiplatformAlertDialog
import io.github.orioneee.presentation.components.buildStringSection
import io.github.orioneee.presentation.components.canSwipePage
import io.github.orioneee.presentation.screens.requests.list.RequestListViewModel
import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.max

internal class RequestDetailsScreen {
    fun getSizeText(size: Long): String {
        return if (
            size < 1024
        ) {
            "$size bytes"
        } else if (size < 1024 * 1024) {
            "${size / 1024} KB"
        } else {
            "${size / (1024 * 1024)} MB"
        }
    }

    @Composable
    fun ChoiceFormatButton(
        selected: BodyType,
        onSelect: (BodyType) -> Unit,
        supportImage: Boolean = true
    ) {
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
        ) {
            BodyType.entries.filter {
                it != BodyType.IMAGE || supportImage
            }.forEachIndexed { index, bodyType ->
                SegmentedButton(
                    selected = selected == bodyType,
                    onClick = {
                        onSelect(bodyType)
                    },
                    label = {
                        Text(bodyType.name)
                    },
                    shape = RoundedCornerShape(
                        topStart = if (index == 0) 16.dp else 0.dp,
                        bottomStart = if (index == 0) 16.dp else 0.dp,
                        topEnd = if (index == BodyType.entries.lastIndex) 16.dp else 0.dp,
                        bottomEnd = if (index == BodyType.entries.lastIndex) 16.dp else 0.dp
                    )
                )
            }
        }
    }

    @Composable
    fun DisplayImportantSection(
        data: List<String>
    ) {
        var isVisibleInfoDialog by remember { mutableStateOf(false) }

        BodySection(
            titleContent = {
                Row(
                    verticalAlignment = Alignment.Companion.CenterVertically,
                ) {
                    Text(
                        buildStringSection(
                            title = stringResource(Res.string.important),
                            content = "",
                            separator = ""
                        )
                    )
                    IconButton(
                        onClick = {
                            isVisibleInfoDialog = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                        )
                    }
                }
            }
        ) {
            SelectionContainer {
                Column(
                    modifier = Modifier.Companion
                        .padding(8.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.Companion.Start,
                ) {
                    data.forEach {
                        Text(it)
                    }
                }
            }
        }

        MultiplatformAlertDialog(
            isShowDialog = isVisibleInfoDialog,
            onDismiss = {
                isVisibleInfoDialog = false
            },
            title = {
                Text(stringResource(Res.string.what_is_important))
            },
            content = {
                Text(stringResource(Res.string.developer_mark_this_as_important))
            }
        )
    }

    @Composable
    fun RequestDetails(
        request: Transaction,
        viewModel: RequestDetailsViewModel
    ) {
        Column(
            modifier = Modifier.Companion
                .padding(horizontal = 8.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Companion.Start,
        ) {
            Spacer(Modifier.Companion.height(8.dp))
            if (request.importantInRequest.isNotEmpty()) {
                DisplayImportantSection(request.importantInRequest)
                Spacer(Modifier.Companion.height(16.dp))
            }
            SelectionContainer {
                Text(buildStringSection(stringResource(Res.string.url), request.fullUrl))
            }
            SelectionContainer {
                Text(buildStringSection(stringResource(Res.string.method), request.method))
            }
            Text(
                buildStringSection(
                    stringResource(Res.string.duration),
                    if (request.responseTime != null) "${request.totalTime} ms" else ""
                )
            )
            if ((request.requestBody?.size ?: 0) > 0) {
                Text(
                    buildStringSection(
                        stringResource(Res.string.request_size),
                        getSizeText(request.requestBody?.size?.toLong() ?: 0L)
                    ),
                )
            }

            if (request.requestHeaders.isNotEmpty()) {
                Spacer(Modifier.Companion.height(16.dp))
                BodySection(
                    title = stringResource(Res.string.headers)
                ) {
                    SelectionContainer {
                        Column(
                            modifier = Modifier.Companion
                                .padding(8.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.Companion.Start,
                        ) {
                            request.requestHeaders.entries.forEach {
                                Text(
                                    buildStringSection(it.key, it.value),
                                )
                            }
                        }
                    }
                }

            }
            if ((request.requestBody?.size ?: 0) > 0) {
                Spacer(Modifier.height(16.dp))
                val selectedFromVm =
                    viewModel.selectedRequestBodyFormat.collectAsStateWithLifecycle()
                val selected =
                    selectedFromVm.value ?: BodyType.JSON
                ChoiceFormatButton(
                    selected = selected,
                    onSelect = {
                        viewModel.onRequestBodyFormatSelected(it)
                    },
                    supportImage = false
                )
                Spacer(Modifier.height(16.dp))
                BodySection {
                    Box(
                        modifier = Modifier.Companion
                            .padding(8.dp)

                    ) {
                        SelectionContainer {
                            val formatted =
                                viewModel.formatedRequestBody.collectAsStateWithLifecycle(
                                    AnnotatedString("")
                                )
                            Text(text = formatted.value ?: AnnotatedString(""))
                        }
                    }
                }
            }
            Spacer(Modifier.Companion.height(8.dp))
        }
    }

    @Composable
    fun ResponseDetails(
        request: Transaction,
        viewModel: RequestDetailsViewModel
    ) {
        Column(
            modifier = Modifier.Companion
                .padding(horizontal = 8.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Companion.Start,
        ) {
            Spacer(Modifier.Companion.height(8.dp))
            if (request.importantInResponse.isNotEmpty()) {
                DisplayImportantSection(request.importantInResponse)
                Spacer(Modifier.Companion.height(16.dp))
            }
            Text(
                buildStringSection(
                    title = stringResource(Res.string.response_size),
                    content = getSizeText(request.responseBody?.size?.toLong() ?: 0)
                )
            )

            Text(
                buildStringSection(
                    title = stringResource(Res.string.status),
                    content = request.responseStatus?.toString()
                        ?: if (request.error != null) stringResource(Res.string.request_failed) else stringResource(
                            Res.string.unknown
                        )
                )
            )
            if (request.responseHeaders.isNotEmpty()) {
                Spacer(Modifier.Companion.height(16.dp))
                BodySection(
                    defaultExpanded = false,
                    title = stringResource(Res.string.headers),
                ) {
                    SelectionContainer {
                        Column(
                            modifier = Modifier.Companion
                                .padding(8.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.Companion.Start,
                        ) {
                            request.responseHeaders.entries.forEach {
                                Text(
                                    buildStringSection(it.key, it.value),
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            if (
                request.responseBody?.isNotEmpty() == true ||
                request.error != null
            ) {
                val selectedFromVm =
                    viewModel.selectedResponseBodyFormat.collectAsStateWithLifecycle()
                val selected =
                    selectedFromVm.value ?: request.responseDefaultType ?: BodyType.RAW_TEXT
                if (request.error == null) {
                    ChoiceFormatButton(
                        selected = selected,
                        onSelect = {
                            viewModel.onResponseBodyFormatSelected(it)
                        }
                    )
                    Spacer(Modifier.height(16.dp))
                }
                BodySection {
                    if (selected != BodyType.IMAGE) {
                        if (request.error == null) {
                            Box(
                                modifier = Modifier.Companion
                                    .padding(8.dp)
                            ) {
                                val formatted =
                                    viewModel.formatedResponseBody.collectAsStateWithLifecycle(
                                        AnnotatedString("")
                                    )
                                SelectionContainer {
                                    Text(formatted.value ?: AnnotatedString(""))
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier.Companion
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                contentAlignment = Alignment.Companion.Center
                            ) {
                                SelectionContainer {
                                    Text(
                                        stringResource(Res.string.error, request.error),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier.Companion
                                .fillMaxWidth()
                                .padding(8.dp),
                            contentAlignment = Alignment.Companion.Center
                        ) {
                            AsyncImage(
                                model = request.responseBody,
                                contentDescription = "Response Image",
                                modifier = Modifier.Companion
                                    .height(300.dp)
                                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                            )
                        }
                    }
                }
                Spacer(Modifier.Companion.height(8.dp))
            }
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Screen(
        navController: NavHostController,
        requestId: Long,
    ) {
        val viewModel: RequestDetailsViewModel = koinViewModel {
            parametersOf(requestId)
        }
        val request by viewModel.requestByID.collectAsState(initial = null)
        LaunchedEffect(request) {
            if (request != null && request?.isViewed != true) {
                viewModel.onViewed(request!!)
            }
        }
        if (request == null) {
            Box(
                modifier = Modifier.Companion.fillMaxSize(),
                contentAlignment = Alignment.Companion.Center
            ) {
                Text(stringResource(Res.string.no_request_found_with_id, requestId))
            }
        } else {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            val title = StringBuilder()
                            if (!request!!.path.contains("/")) title.append("/")
                            title.append(request!!.path)
                            if (request!!.responseStatus != null) {
                                title.append(" - ${request!!.responseStatus}")
                            }

                            Text(
                                title.toString(),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.titleSmall
                            )
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    navController.popBackStack()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBackIosNew,
                                    contentDescription = "Back"
                                )
                            }
                        },
                    )
                },
            ) {
                Column(
                    modifier = Modifier.Companion
                        .padding(it)
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.Companion.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    val scope = rememberCoroutineScope()
                    val pager = rememberPagerState(
                        initialPage = 1,
                        pageCount = {
                            2
                        }
                    )
                    TabRow(
                        selectedTabIndex = pager.currentPage,
                        modifier = Modifier.Companion.fillMaxWidth()
                    ) {
                        Tab(
                            modifier = Modifier.Companion.clip(
                                RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp
                                )
                            ),
                            selected = pager.currentPage == 0,
                            onClick = {
                                scope.launch {
                                    pager.animateScrollToPage(0)
                                }
                            },
                            text = { Text(stringResource(Res.string.request_tab)) }
                        )
                        Tab(
                            modifier = Modifier.Companion.clip(
                                RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp
                                )
                            ),
                            selected = pager.currentPage == 1,
                            onClick = {
                                scope.launch {
                                    pager.animateScrollToPage(1)
                                }
                            },
                            text = { Text(stringResource(Res.string.response_tab)) }
                        )
                    }
                    HorizontalPager(
                        userScrollEnabled = canSwipePage,
                        state = pager
                    ) {
                        when (it) {
                            0 -> RequestDetails(request!!, viewModel)
                            1 -> ResponseDetails(request!!, viewModel)
                        }
                    }
                }
            }
        }
    }
}