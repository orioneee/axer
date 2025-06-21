package com.oriooneee.ktorin.presentation.screens.details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.oriooneee.ktorin.presentation.components.CustomAlertDialog
import com.oriooneee.ktorin.presentation.navigation.Routes
import com.oriooneee.ktorin.presentation.screens.RequestViewModel
import com.oriooneee.ktorin.room.entities.Transaction
import dev.snipme.highlights.Highlights
import generateAnnotatedString
import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

class RequestDetailsScreen {
    fun buildStringSection(
        title: String,
        content: String
    ) = buildAnnotatedString {
        withStyle(
            style = SpanStyle(
                fontWeight = FontWeight.Bold
            )
        ) {
            append("$title: ")
        }
        append(content)
    }

    @Composable
    fun DisplayImportantSection(
        data: List<String>
    ) {
        var isExpanded by remember { mutableStateOf(false) }
        val animatedRotation by animateFloatAsState(if (isExpanded) 180f else 0f)
        var isVisibleInfoDialog by remember { mutableStateOf(false) }
        CustomAlertDialog(
            isShowDialog = isVisibleInfoDialog,
            onDismiss = {
                isVisibleInfoDialog = false
            }
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .width(IntrinsicSize.Min)
            ) {
                Text("Why this data is important?", fontSize = 24.sp)
                Text("Developer mark this data as important")
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .width(IntrinsicSize.Max),
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            buildStringSection(
                                title = "Important",
                                content = ""
                            )
                        )
                        IconButton(
                            onClick = {

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

    @Composable
    fun RequestDetails(
        request: Transaction,
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start,
        ) {
            if (request.importantInRequest.isNotEmpty()) {
                DisplayImportantSection(request.importantInRequest)
                Spacer(Modifier.height(16.dp))
            }
            Text(buildStringSection("Url", request.fullUrl))
            Text(buildStringSection("Method", request.method))
            Text(
                buildStringSection(
                    "Duration",
                    if (request.responseTime != null) "${request.totalTime} ms" else ""
                )
            )
            Text(
                buildStringSection(
                    "Request size",
                    if (request.requestBody != null) "${request.requestBody.toByteArray().size} bytes" else "0 bytes"
                ),
            )
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
                            Text(request.requestBody)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ResponseDetails(
        request: Transaction,
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start,
        ) {
            if (request.importantInRequest.isNotEmpty()) {
                DisplayImportantSection(request.importantInResponse)
                Spacer(Modifier.height(16.dp))
            }
            val size = kotlin.math.max(
                request.responseBody?.toByteArray()?.size ?: 0,
                request.imageBytes?.size ?: 0
            )
            val sizeText = if (
                size < 1024
            ) {
                "$size bytes"
            } else if (size < 1024 * 1024) {
                "${size / 1024} KB"
            } else {
                "${size / (1024 * 1024)} MB"
            }
            Text(
                buildStringSection(
                    title = "Response size",
                    content = sizeText
                )
            )

            Text(
                buildStringSection(
                    title = "Status",
                    content = request.responseStatus?.toString() ?: "No response"
                )
            )
            if (request.responseHeaders.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                HeaderSection(request.responseHeaders)
            }
            Spacer(Modifier.height(16.dp))
            if (request.responseBody?.isNotBlank() == true || request.imageBytes?.isNotEmpty() == true) {
                BodySection {
                    if (request.isImage != true) {
                        Box(
                            modifier = Modifier
                                .padding(8.dp)

                        ) {
                            val highlights by remember {
                                mutableStateOf(
                                    Highlights
                                        .Builder(code = request.responseBody ?: "")
                                        .build()
                                )
                            }
                            var textState by remember {
                                mutableStateOf(AnnotatedString(highlights.getCode()))
                            }

                            LaunchedEffect(highlights) {
                                textState = highlights
                                    .getHighlights()
                                    .generateAnnotatedString(highlights.getCode())
                            }
                            SelectionContainer {
                                Text(text = textState)
                            }
                        }
                    } else {
                        AsyncImage(
                            model = request.imageBytes,
                            contentDescription = "Response Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                        )
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
        val request by viewModel.requestByID.collectAsState(initial = null)
        if (request == null) {
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
                                request!!.path + if (request?.responseStatus != null) {
                                    " - ${request!!.responseStatus}"
                                } else {
                                    ""
                                }
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
                        actions = {
                            IconButton(
                                onClick = {
                                    navController.navigate(Routes.SANDBOX.route + "/${request!!.id}")
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CallMade,
                                    contentDescription = "Back"
                                )
                            }
                        }
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
                        initialPage = if (request!!.method == "GET") 1 else 0,
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
                            0 -> RequestDetails(request = request!!)
                            1 -> ResponseDetails(request = request!!)
                        }
                    }
                }
            }
        }
    }
}