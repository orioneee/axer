package io.github.orioneee.presentation.selectdevice

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.Router
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import io.github.orioneee.SupportingVersions
import io.github.orioneee.domain.other.DeviceData
import io.github.orioneee.presentation.screens.requests.EmptyScreen

class SelectDeviceScreen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Screen(
        navController: NavHostController,
    ) {
        val viewModel: DeviceScanViewModel = viewModel(
            factory = viewModelFactory {
                initializer {
                    DeviceScanViewModel()
                }
            }
        )
        val uiState by viewModel.uiState.collectAsState()
        LaunchedEffect(Unit) {
            viewModel.startScanning()
        }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Select a Device") },
                    actions = {
                        if (!uiState.isSearching) {
                            IconButton(onClick = { viewModel.startScanning() }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Rescan")
                            }
                        }
                    },
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier.Companion.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.Companion.CenterHorizontally
            ) {
                AnimatedVisibility(
                    uiState.isSearching
                ) {
                    ScanningProgress(uiState.progress)
                }

                if (uiState.devices.isEmpty()) {
                    if (!uiState.isSearching) {
                        Box(
                            modifier = Modifier.Companion.weight(1f),
                            contentAlignment = Alignment.Companion.Center
                        ) {
                            EmptyState()
                        }
                    } else {
                        Box(
                            modifier = Modifier.Companion.weight(1f).padding(horizontal = 32.dp),
                            contentAlignment = Alignment.Companion.Center
                        ) {
                            Text(
                                "Scanning for devices on your network...",
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Companion.Center,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                } else {
                    DeviceList(devices = uiState.devices) {
                        navController.navigate(it)
                    }
                }
            }
        }
    }

    /**
     * A composable that shows a determinate progress bar and percentage text.
     * To be displayed at the top of the screen during a scan.
     */
    @Composable
    fun ScanningProgress(progress: Float) {
        val animatedProgress by animateFloatAsState(
            targetValue = progress, label = "ProgressAnimation"
        )

        Column(
            modifier = Modifier.Companion.fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.Companion.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // The progress property makes this a determinate indicator.
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Progress: ${(animatedProgress * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }

    data class SetupStep(
        val icon: ImageVector,
        val text: String
    )

    // This logic remains the same
    val lastSupportedVersion = SupportingVersions.list.lastOrNull() ?: "1.0.0"

    // Populate the list using our new data class
    val steps = listOf(
        SetupStep(
            icon = Icons.Default.Dns,
            text = "Make sure axer server is running on your app."
        ),
        SetupStep(
            icon = Icons.Default.Wifi,
            text = "Ensure your device is connected to the same network as the device running the debugger."
        ),
        SetupStep(
            icon = Icons.Default.SystemUpdate,
            text = "Ensure your device is running Axer version $lastSupportedVersion or later."
        )
    )

    @Composable
    fun EmptyState() {

        EmptyScreen().Screen(
            image = rememberVectorPainter(Icons.Outlined.WifiOff),
            title = {
                Text(
                    text = "No Devices Found",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            description = {
                Column {
                    steps.forEachIndexed { index, it ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(10.dp)
                        ) {
                            Icon(
                                imageVector = it.icon,
                                contentDescription = null,
                                modifier = Modifier.Companion.size(24.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = it.text,
                                textAlign = TextAlign.Start,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }
        )
    }

    @Composable
    fun DeviceList(devices: List<DeviceData>, onDeviceClick: (DeviceData) -> Unit) {
        LazyVerticalStaggeredGrid(
            modifier = Modifier.Companion.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            columns = StaggeredGridCells.Adaptive(300.dp)
        ) {
            items(items = devices, key = { it.ip ?: it.deviceName }) { device ->
                DeviceCard(device) {
                    onDeviceClick(device)
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun DeviceCard(
        deviceData: DeviceData, onClick: () -> Unit
    ) {
        Card(
            onClick = onClick,
            modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier.Companion.padding(20.dp),
                verticalAlignment = Alignment.Companion.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Router,
                    contentDescription = "Device Icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.Companion.size(40.dp)
                )
                Spacer(Modifier.Companion.width(20.dp))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = deviceData.readableDeviceName ?: "Unknown Device",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "OS: ${deviceData.osName ?: "N/A"} ${deviceData.osVersion ?: ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "IP: ${deviceData.ip ?: "No IP"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "Version: ${deviceData.axerVersion}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}