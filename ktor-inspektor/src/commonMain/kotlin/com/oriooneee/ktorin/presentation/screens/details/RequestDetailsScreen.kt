package com.oriooneee.ktorin.presentation.screens.details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.oriooneee.ktorin.domain.HighlightedBodyWrapper
import com.oriooneee.ktorin.presentation.components.CustomAlertDialog
import com.oriooneee.ktorin.presentation.screens.RequestViewModel
import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

class RequestDetailsScreen {
    fun buildStringSection(
        title: String,
        content: String,
        separator: String = ": "
    ) = buildAnnotatedString {
        withStyle(
            style = SpanStyle(
                fontWeight = FontWeight.Bold
            )
        ) {
            append(title + separator)
        }
        append(content)
    }

    @Composable
    fun DisplayImportantSection(
        data: List<String>
    ) {
        var isExpanded by remember { mutableStateOf(true) }
        val animatedRotation by animateFloatAsState(if (isExpanded) 180f else 0f)
        var isVisibleInfoDialog by remember { mutableStateOf(false) }
        Card {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            isExpanded = !isExpanded
                        }
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            buildStringSection(
                                title = "Important",
                                content = "",
                                separator = ""
                            )
                        )
                        IconButton(
                            onClick = {
                                isVisibleInfoDialog = true
                            }
                        ) {
                            Image(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null
                            )
                        }
                    }
                    Image(
                        modifier = Modifier.rotate(animatedRotation),
                        imageVector = Icons.Outlined.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
                AnimatedVisibility(
                    visible = isExpanded
                ) {
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                    ) {
                        SelectionContainer {
                            Column(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.Start,
                            ) {
                                data.forEach {
                                    Text(it)
                                }
                            }
                        }
                    }
                }
            }
        }
        CustomAlertDialog(
            isShowDialog = isVisibleInfoDialog,
            onDismiss = {
                isVisibleInfoDialog = false
            }
        ) {
            Surface(
                modifier = Modifier,
//                    .width(450.dp)
                shape = RoundedCornerShape(12.dp),
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp),
                ) {
                    Text("Developer mark this as important")
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                isVisibleInfoDialog = false
                            }
                        ) {
                            Text("Close")
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun HeaderSection(headers: Map<String, String>) {
        var isExpanded by remember { mutableStateOf(false) }
        val animatedRotation by animateFloatAsState(if (isExpanded) 180f else 0f)
        Card {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            isExpanded = !isExpanded
                        }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        buildStringSection(
                            title = "Headers",
                            content = ""
                        )
                    )
                    Image(
                        modifier = Modifier.rotate(animatedRotation),
                        imageVector = Icons.Outlined.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
                AnimatedVisibility(
                    visible = isExpanded
                ) {
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                    ) {
                        SelectionContainer {
                            Column(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.Start,
                            ) {
                                headers.entries.forEach {
                                    Text(
                                        buildStringSection(it.key, it.value),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun BodySection(
        content: @Composable () -> Unit
    ) {
        var isExpanded by remember { mutableStateOf(true) }
        val animatedRotation by animateFloatAsState(if (isExpanded) 180f else 0f)
        Card {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            isExpanded = !isExpanded
                        }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        buildStringSection(
                            title = "Body",
                            content = ""
                        )
                    )
                    Image(
                        modifier = Modifier.rotate(animatedRotation),
                        imageVector = Icons.Outlined.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
                AnimatedVisibility(
                    visible = isExpanded
                ) {
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                    ) {
                        content()
                    }
                }
            }
        }
    }

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
    fun RequestDetails(
        wrapped: HighlightedBodyWrapper,
    ) {
        val request = wrapped.request
        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start,
        ) {
            Spacer(Modifier.height(8.dp))
            if (request.importantInRequest.isNotEmpty()) {
                DisplayImportantSection(request.importantInRequest)
                Spacer(Modifier.height(16.dp))
            }
            SelectionContainer {
                Text(buildStringSection("Url", request.fullUrl))
            }
            SelectionContainer {
                Text(buildStringSection("Method", request.method))
            }
            Text(
                buildStringSection(
                    "Duration",
                    if (request.responseTime != null) "${request.totalTime} ms" else ""
                )
            )
            if ((request.requestBody?.toByteArray()?.size ?: 0) > 0) {
                Text(
                    buildStringSection(
                        "Request size",
                        getSizeText(request.requestBody?.toByteArray()?.size?.toLong() ?: 0L)
                    ),
                )
            }

            if (request.requestHeaders.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                HeaderSection(request.requestHeaders)

            }
            if (!request.requestBody.isNullOrBlank()) {
                Spacer(Modifier.height(16.dp))
                BodySection {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)

                    ) {
                        SelectionContainer {
                            Text(text = wrapped.highlightedRequestBody)
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }

    @Composable
    fun ResponseDetails(
        wrapped: HighlightedBodyWrapper
    ) {
        val request = wrapped.request
        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start,
        ) {
            Spacer(Modifier.height(8.dp))
            if (request.importantInResponse.isNotEmpty()) {
                DisplayImportantSection(request.importantInResponse)
                Spacer(Modifier.height(16.dp))
            }
            val size = kotlin.math.max(
                request.responseBody?.toByteArray()?.size ?: 0,
                request.imageBytes?.size ?: 0
            ).toLong()

            Text(
                buildStringSection(
                    title = "Response size",
                    content = getSizeText(size)
                )
            )

            Text(
                buildStringSection(
                    title = "Status",
                    content = request.responseStatus?.toString() ?: if( request.error != null) "Request failed" else "Unknown"
                )
            )
            if (request.responseHeaders.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                HeaderSection(request.responseHeaders)
            }
            Spacer(Modifier.height(16.dp))
            if (request.responseBody?.isNotBlank() == true || request.imageBytes?.isNotEmpty() == true || !request.error.isNullOrBlank()) {
                BodySection {
                    if (request.isImage != true) {
                        if (request.error == null) {
                            Box(
                                modifier = Modifier
                                    .padding(8.dp)

                            ) {
                                SelectionContainer {
                                    Text(wrapped.highlightedResponseBody)
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                SelectionContainer {
                                    Text(
                                        "Error: ${request.error}",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = request.imageBytes,
                                contentDescription = "Response Image",
                                modifier = Modifier
                                    .height(300.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Screen(
        navController: NavHostController,
        requestId: Long,
    ) {
        val viewModel: RequestViewModel = koinViewModel {
            parametersOf(requestId)
        }
        val wrappped by viewModel.requestByID.collectAsState(initial = null)
        LaunchedEffect(wrappped) {
            if(wrappped != null && !wrappped!!.request.isViewed){
                viewModel.onViewed(wrappped!!.request)
            }
        }
        if (wrappped == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No request found with ID: $requestId")
            }
        } else {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                wrappped!!.request.path + if (wrappped!!.request.responseStatus != null) {
                                    " - ${wrappped!!.request.responseStatus}"
                                } else {
                                    ""
                                },
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
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
//                        actions = {
//                            IconButton(
//                                onClick = {
//                                    navController.navigate(Routes.SANDBOX.route + "/${request!!.id}")
//                                }
//                            ) {
//                                Icon(
//                                    imageVector = Icons.Default.CallMade,
//                                    contentDescription = "Back"
//                                )
//                            }
//                        }
                    )
                },
            ) {
                Column(
                    modifier = Modifier
                        .padding(it)
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
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
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Tab(
                            modifier = Modifier.clip(
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
                            text = { Text("Request") }
                        )
                        Tab(
                            modifier = Modifier.clip(
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
                            text = { Text("Response") }
                        )
                    }
                    HorizontalPager(
                        state = pager
                    ) {
                        when (it) {
                            0 -> RequestDetails(wrappped!!)
                            1 -> ResponseDetails(wrappped!!)
                        }
                    }
                }
            }
        }
    }
}