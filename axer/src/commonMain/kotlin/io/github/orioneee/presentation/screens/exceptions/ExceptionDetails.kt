package io.github.orioneee.presentation.screens.exceptions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import io.github.orioneee.formateAsTime
import io.github.orioneee.presentation.components.BodySection
import io.github.orioneee.presentation.components.buildStringSection
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

internal class ExceptionDetails {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Screen(
        navController: NavHostController,
        exceptionID: Long,
    ) {
        val viewModel: ExceptionsViewModel = koinViewModel {
            parametersOf(exceptionID)
        }
        val exception by viewModel.exceptionByID.collectAsState(initial = null)
        if (exception == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No exception found with ID: $exceptionID")
            }
        } else {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                "${exception!!.shortName} - ${exception!!.time.formateAsTime()}",
                                maxLines = 1,
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
                    )
                },
            ) {
                Column(
                    modifier = Modifier
                        .padding(it)
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState())
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Top
                ) {
                    Spacer(Modifier.height(8.dp))
                    SelectionContainer {
                        Text(
                            buildStringSection(
                                "Name",
                                exception!!.shortName,
                            )
                        )
                    }
                    SelectionContainer {
                        Text(
                            buildStringSection(
                                "Message",
                                exception!!.message,
                            )
                        )
                    }
                    SelectionContainer {
                        Text(
                            buildStringSection(
                                "Time",
                                exception!!.time.formateAsTime(),
                            )
                        )
                    }
                    if (exception!!.stackTrace.isNotBlank()) {
                        Spacer(Modifier.height(16.dp))
                        BodySection(
                            title = "Stack Trace"
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(8.dp)

                            ) {
                                SelectionContainer {
                                    Text(text = exception!!.stackTrace)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}