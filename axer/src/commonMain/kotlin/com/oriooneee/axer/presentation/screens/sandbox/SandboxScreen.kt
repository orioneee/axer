package com.oriooneee.axer.presentation.screens.sandbox

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

class SandboxScreen {

    @Composable
    fun HeaderItem(
        key: String,
        value: String,
        onUpdate: (String, String) -> Unit
    ) {
        var isVisible by remember { mutableStateOf(key.isNotBlank() && value.isNotBlank()) }
        LaunchedEffect(Unit) {
            isVisible = true
        }
        AnimatedVisibility(
            visible = isVisible,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = key,
                    onValueChange = { newKey ->
                        onUpdate(newKey, value)
                    },
                    modifier = Modifier.width(120.dp)
                )
                Text(":", fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = value,
                    onValueChange = { newValue ->
                        onUpdate(key, newValue)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Screen(
        navController: NavHostController,
        requestId: Long,
    ) {
        val viewModel: SandboxViewModel = koinViewModel {
            parametersOf(requestId)
        }
        val request by viewModel.requestByID.collectAsState(null)
        val currentUrl by viewModel.selectedUrl.collectAsState()
        val currentHeaders by viewModel.selectedHeaders.collectAsState(emptyMap())
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text("Sandbox")
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
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("GET", fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = currentUrl,
                        onValueChange = {
                            viewModel.setUrl(it)
                        },
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .fillMaxWidth()
                    )
                    Button(
                        onClick = {

                        }
                    ) {
                        Text("Send")
                    }
                }
                currentHeaders.toList().forEachIndexed { index, (key, value) ->
                    HeaderItem(
                        key = key,
                        value = value,
                        onUpdate = { newKey, newValue ->
                            viewModel.setHeader(
                                index = index,
                                key = newKey,
                                value = newValue
                            )
                        }
                    )
                }
            }
        }
    }
}