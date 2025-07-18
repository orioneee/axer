package io.github.orioneee

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Router
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import io.github.orioneee.domain.other.DeviceData
import io.github.orioneee.navigation.Route
import io.github.orioneee.presentation.selectdevice.DeviceScanViewModel


class SelectDeviceScreen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Screen(
        navController: NavHostController, // Assuming this is passed in
    ) {
        val viewModel: DeviceScanViewModel = viewModel(
            factory = viewModelFactory {
                initializer {
                    DeviceScanViewModel()
                }
            }
        )
        val uiState by viewModel.uiState.collectAsState()

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Select a Device") }, actions = {
                        if (!uiState.isSearching) {
                            IconButton(onClick = { viewModel.startScanning() }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Rescan")
                            }
                        }
                    }, colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (uiState.isSearching) {
                    ScanningProgress(uiState.progress)
                }

                if (uiState.devices.isEmpty()) {
                    if (!uiState.isSearching) {
                        Box(
                            modifier = Modifier.weight(1f), contentAlignment = Alignment.Center
                        ) {
                            EmptyState { viewModel.startScanning() }
                        }
                    } else {
                        Box(
                            modifier = Modifier.weight(1f).padding(horizontal = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Scanning for devices on your network...",
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                } else {
                    DeviceList(devices = uiState.devices) { ip ->
                        navController.navigate("${Route.DEVICE_INSPECTION.path}/$ip")
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
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // The progress property makes this a determinate indicator.
            LinearProgressIndicator(
                progress = { animatedProgress }, modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Progress: ${(animatedProgress * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }


    @Composable
    fun EmptyState(onRetry: () -> Unit) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.WifiOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.size(80.dp)
            )
            Spacer(Modifier.height(24.dp))
            Text(
                "No Devices Found", style = MaterialTheme.typography.headlineSmall
            )
            Text(
                "Please ensure your device is on the same Wi-Fi network and try again.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Button(onClick = onRetry) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Try Again")
            }
        }
    }

    @Composable
    fun DeviceList(devices: List<DeviceData>, onDeviceClick: (String) -> Unit) {
        LazyVerticalStaggeredGrid(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            columns = StaggeredGridCells.Adaptive(300.dp)
        ) {
            items(items = devices, key = { it.ip ?: it.deviceName }) { device ->
                DeviceCard(device) {
                    onDeviceClick(device.ip ?: "")
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
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Router,
                    contentDescription = "Device Icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(Modifier.width(20.dp))
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
                }
            }
        }
    }
}