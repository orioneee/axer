package io.github.orioneee.presentation.selectdevice

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material.icons.outlined.BrowserUpdated
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.LinkOff
import androidx.compose.material.icons.outlined.Router
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import io.github.orioneee.AXER_SERVER_PORT
import io.github.orioneee.internal.domain.other.Theme
import io.github.orioneee.internal.presentation.components.AxerLogo
import io.github.orioneee.internal.presentation.components.AxerLogoDialog
import io.github.orioneee.internal.presentation.components.AxerTheme.dark
import io.github.orioneee.internal.presentation.components.AxerTheme.light
import io.github.orioneee.internal.presentation.components.MultiplatformAlertDialog
import io.github.orioneee.internal.presentation.screens.requests.EmptyScreen
import io.github.orioneee.internal.storage.AxerSettings
import io.github.orioneee.models.AdbDevice
import io.github.orioneee.models.ConnectionInfo
import io.github.orioneee.models.Device
import io.github.orioneee.utils.JetbrainsMonoFontFamily
import kotlinx.coroutines.channels.Channel

class SelectDeviceScreen {

    fun validatePort(port: String): String? {
        return when {
            port.isBlank() -> "Port is required"
            port.toIntOrNull() == null -> "Port must be a number"
            port.toInt() !in 1..65535 -> "Port must be 1–65535"
            else -> null
        }
    }

    @Composable
    fun ManualConnectionDialog(
        isShown: Boolean,
        defaultIp: String,
        defaultPort: String = AXER_SERVER_PORT.toString(),
        onDismiss: () -> Unit,
        onAdd: (ip: String, port: String) -> Unit
    ) {
        var ip by remember { mutableStateOf(defaultIp) }
        var port by remember { mutableStateOf(defaultPort) }

        var ipError by remember { mutableStateOf<String?>(null) }
        var portError by remember { mutableStateOf<String?>(null) }

        // Validation functions
        fun validateIp(ip: String): String? {
            val ipv4Regex = Regex(
                "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}" + "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
            )
            return if (ip.matches(ipv4Regex)) null else "Invalid IP address"
        }


        val isFormValid = validateIp(ip) == null && validatePort(port) == null

        MultiplatformAlertDialog(
            isShowDialog = isShown,
            onDismiss = onDismiss,
            title = {
                Text(
                    text = "Manual Connection",
                    fontFamily = JetbrainsMonoFontFamily,
                    style = MaterialTheme.typography.titleLarge,
                )
            },
            content = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = ip,
                        onValueChange = {
                            ip = it
                            ipError = validateIp(it)
                        },
                        label = {
                            Text(
                                "IP Address",
                                fontFamily = JetbrainsMonoFontFamily,
                            )
                        },
                        isError = ipError != null,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (ipError != null) {
                        Text(
                            text = ipError!!,
                            fontFamily = JetbrainsMonoFontFamily,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    OutlinedTextField(
                        value = port,
                        onValueChange = {
                            port = it
                            portError = validatePort(it)
                        },
                        label = {
                            Text(
                                "Port",
                                fontFamily = JetbrainsMonoFontFamily,
                            )
                        },
                        isError = portError != null,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (portError != null) {
                        Text(
                            text = portError!!,
                            fontFamily = JetbrainsMonoFontFamily,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }, confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.weight(1f))

                    TextButton(
                        onClick = onDismiss,
                    ) {
                        Text(
                            "Dismiss",
                            fontFamily = JetbrainsMonoFontFamily,
                        )
                    }

                    Button(
                        onClick = {
                            ipError = validateIp(ip)
                            portError = validatePort(port)

                            if (isFormValid) {
                                onAdd(ip.trim(), port.trim())
                            }
                        }, enabled = isFormValid
                    ) {
                        Text(
                            "Add",
                            fontFamily = JetbrainsMonoFontFamily,
                        )
                    }
                }
            })
    }


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
        val isShown = viewModel.isShowingNewVersionDialog.collectAsState(false)
        val isShowAddDeviceDialog = viewModel.isShowAddDeviceDialog.collectAsState(false)
        val scanningProgress = viewModel.scanningProgress.collectAsStateWithLifecycle(0f)
        val foundedDevices = viewModel.foundedDevices.collectAsStateWithLifecycle(emptyList())
        val addedButNotAxer =
            viewModel.manuallyAddedButNotAxer.collectAsStateWithLifecycle(emptyList())
        val adbDevices = viewModel.adbDevices.collectAsStateWithLifecycle(emptyList())


        DisposableEffect(Unit) {
            viewModel.startScanning()
            onDispose {
                viewModel.stopScanning()
            }
        }

        ManualConnectionDialog(
            isShown = isShowAddDeviceDialog.value,
            onDismiss = viewModel::onDismissAddDevice,
            defaultIp = viewModel.generateConnections().let {

                val conn = it.firstOrNull { it.ip.startsWith("192") } ?: it.firstOrNull()
                val ip = conn?.ip ?: "127.0.0.1"
                ip.substringBeforeLast(".").let {
                    if (!it.endsWith(".")) "$it." else it
                }
            },
            onAdd = { ip, port ->
                viewModel.onAddedConnection(
                    ConnectionInfo(
                        ip = ip, port = port.toInt(), isCustomAdded = true
                    )
                )
            })
        MultiplatformAlertDialog(
            isShowDialog = isShown.value,
            onDismiss = viewModel::onDismissNewVersion,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AxerLogo(modifier = Modifier.size(32.dp))
                    Text(
                        fontFamily = JetbrainsMonoFontFamily,
                        text = "New Version Available",
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            },
            content = {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Outlined.BrowserUpdated,
                        contentDescription = "Update Icon",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "A New Version is Available",
                        fontFamily = JetbrainsMonoFontFamily,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Please update to the latest version for new features and improvements.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = JetbrainsMonoFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(
                        onClick = viewModel::onDismissNewVersion,
                    ) {
                        Text(
                            "Later",
                            fontFamily = JetbrainsMonoFontFamily,
                        )
                    }
                    val uriHandler = LocalUriHandler.current
                    Button(
                        onClick = {
                            uriHandler.openUri("https://github.com/orioneee/Axer/releases")
                        }) {
                        Text(
                            "Update",
                            fontFamily = JetbrainsMonoFontFamily,
                        )
                    }
                }
            }
        )

        val currentTheme by AxerSettings.themeFlow.collectAsState(AxerSettings.theme.get())
        val isDark = when (currentTheme) {
            Theme.FOLLOW_SYSTEM -> isSystemInDarkTheme()
            Theme.LIGHT -> false
            Theme.DARK -> true
        }
        val scheme = if (isDark) dark else light
        val colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = scheme.surface,
            titleContentColor = scheme.onSurface,
            actionIconContentColor = scheme.onSurface,
            navigationIconContentColor = scheme.onSurface
        )

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    colors = colors,
                    title = {
                        Text(
                            "Select a Device",
                            fontFamily = JetbrainsMonoFontFamily,
                        )
                    },
                    navigationIcon = {
                        AxerLogoDialog()
                    },
                    actions = {
                        IconButton(
                            onClick = viewModel::onClickAddDevice
                        ) {
                            Icon(
                                Icons.Outlined.Add,
                                contentDescription = "Add Device",
                            )
                        }
                        IconButton(
                            onClick = {
                                viewModel.restartScanning()
                            }
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Rescan")
                        }
                    },
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier.Companion.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.Companion.CenterHorizontally
            ) {
                if (foundedDevices.value.isEmpty() && addedButNotAxer.value.isEmpty() && adbDevices.value.isEmpty()) {
                    Box(
                        modifier = Modifier.Companion.weight(1f),
                        contentAlignment = Alignment.Companion.Center
                    ) {
                        EmptyState()
                    }
                } else {
                    DeviceList(
                        viewModel = viewModel,
                        devices = foundedDevices.value,
                        addedButNotAxer = addedButNotAxer.value,
                        onDeviceClick = { device ->
                            navController.navigate(device.toNavArguments())
                        },
                        onDelete = { conn ->
                            println("Deleting connection: $conn")
                            viewModel.onDeleteConnection(conn)
                        },
                        navController = navController
                    )
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
                fontFamily = JetbrainsMonoFontFamily,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }

    data class SetupStep(
        val icon: ImageVector, val text: String
    )

    val steps = listOf(
        SetupStep(
            icon = Icons.Default.Dns, text = "Make sure axer server is running on your app."
        ),
        SetupStep(
            icon = Icons.Default.Wifi,
            text = "Ensure your device is connected to the same network as the device running the debugger."
        ),
    )

    @Composable
    fun EmptyState() {
        EmptyScreen().Screen(image = rememberVectorPainter(Icons.Outlined.WifiOff), title = {
            Text(
                text = "No Devices Found",
                style = MaterialTheme.typography.titleLarge,
                fontFamily = JetbrainsMonoFontFamily,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        }, description = {
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
                            fontFamily = JetbrainsMonoFontFamily,
                            textAlign = TextAlign.Start,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
            }
        })
    }

    @Composable
    fun DeviceList(
        viewModel: DeviceScanViewModel,
        devices: List<Device>,
        onDeviceClick: (Device) -> Unit,
        addedButNotAxer: List<ConnectionInfo>,
        onDelete: (ConnectionInfo) -> Unit,
        navController: NavHostController
    ) {
        val isShowingAddedButNotAxer = addedButNotAxer.isNotEmpty()
        val adbDevices = viewModel.adbDevices.collectAsStateWithLifecycle(emptyList())
        LazyVerticalStaggeredGrid(
            modifier = Modifier.Companion.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            columns = StaggeredGridCells.Adaptive(300.dp)
        ) {
            if (devices.isNotEmpty()) {
                item(
                    span = StaggeredGridItemSpan.FullLine
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Axer devices in local network",
                            fontFamily = JetbrainsMonoFontFamily,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(8.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            items(
                items = devices.toList()
            ) { device ->
                DeviceCard(device, onClick = { onDeviceClick(device) }, onDelete = {
                    onDelete(device.connection)
                }
                )
            }
            if (isShowingAddedButNotAxer) {
                item(
                    span = StaggeredGridItemSpan.FullLine
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Manually added but not recognized as Axer devices, try different IP/port or retry scanning.",
                            fontFamily = JetbrainsMonoFontFamily,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(8.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                items(
                    addedButNotAxer,
                    key = { it.toAddress() }
                ) {
                    ListItem(
                        headlineContent = {
                            Text(
                                text = it.toAddress(),
                                fontFamily = JetbrainsMonoFontFamily,
                                style = MaterialTheme.typography.titleMedium
                            )
                        },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Outlined.LinkOff,
                                contentDescription = "Device Icon",
                            )
                        },
                        trailingContent = {
                            IconButton(
                                onClick = { onDelete(it) }) {
                                Icon(
                                    Icons.Outlined.Delete,
                                    contentDescription = "Delete Device",
                                )
                            }
                        },
                    )
                }
            }
            if (adbDevices.value.isNotEmpty()) {
                item(
                    span = StaggeredGridItemSpan.FullLine
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Devices connected via ADB",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = JetbrainsMonoFontFamily,
                            modifier = Modifier.padding(8.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                items(
                    adbDevices.value,
                    key = { it.serial }
                ) {
                    AdbDevice(
                        viewModel = viewModel,
                        device = it,
                        navController = navController
                    )
                }
            }
        }
    }

    @Composable
    fun AdbDevice(
        viewModel: DeviceScanViewModel,
        device: AdbDevice,
        navController: NavHostController,
    ) {
        var isShowSelectPortDialog by remember { mutableStateOf(false) }
        var port by remember { mutableStateOf(AXER_SERVER_PORT.toString()) }
        var portError by remember { mutableStateOf<String?>(null) }
        val chanel: Channel<Result<Device>> = remember { Channel(Channel.BUFFERED) }
        var isLoading by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            for (connection in chanel) {
                connection.onSuccess {
                    navController.navigate(it.toNavArguments())
                }
                connection.onFailure {
                    portError = "Axer server not found on this device at port $port"
                }
                isLoading = false
            }
        }
        DisposableEffect(Unit) {
            onDispose {
                chanel.close()
            }
        }
        MultiplatformAlertDialog(
            isShowDialog = isShowSelectPortDialog,
            onDismiss = {
                isShowSelectPortDialog
            },
            title = {
                Text(
                    text = "Select Port",
                    fontFamily = JetbrainsMonoFontFamily,
                    style = MaterialTheme.typography.titleLarge,
                )
            },
            content = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AnimatedVisibility(isLoading) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        "Enter the port number for the Axer server on this device, by default it is ${AXER_SERVER_PORT}. ",
                        fontFamily = JetbrainsMonoFontFamily,
                    )
                    OutlinedTextField(
                        enabled = !isLoading,
                        value = port,
                        onValueChange = {
                            port = it
                            portError = validatePort(it)
                        },
                        label = {
                            Text(
                                "Port",
                                fontFamily = JetbrainsMonoFontFamily,
                            )
                        },
                        isError = portError != null,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    portError?.let {
                        Text(
                            text = it,
                            fontFamily = JetbrainsMonoFontFamily,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        enabled = !isLoading,
                        onClick = {
                            isShowSelectPortDialog = false
                            port = AXER_SERVER_PORT.toString()
                            portError = null
                        },
                    ) {
                        Text(
                            "Dismiss",
                            fontFamily = JetbrainsMonoFontFamily,

                            )
                    }

                    Button(
                        onClick = {
                            viewModel.checkAdbDevice(
                                device = device,
                                port = port.toIntOrNull() ?: AXER_SERVER_PORT,
                                channel = chanel
                            )
                        },
                        enabled = !isLoading && portError == null && port.isNotBlank() && port.toIntOrNull() != null && port.toInt() in 1..65535
                    ) {
                        Text(
                            "Add",
                            fontFamily = JetbrainsMonoFontFamily,
                        )
                    }
                }
            }
        )
        ListItem(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                    isShowSelectPortDialog = true
                },
            headlineContent = {
                Text(
                    text = device.name,
                    fontFamily = JetbrainsMonoFontFamily,
                )
            },
            supportingContent = {
                Text(
                    text = device.serial,
                    fontFamily = JetbrainsMonoFontFamily,
                )
            },
            leadingContent = {
                Icon(
                    imageVector = Icons.Outlined.Android,
                    contentDescription = "Device Icon",
                )
            },
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun DeviceCard(
        device: Device, onClick: () -> Unit, onDelete: () -> Unit
    ) {
        Card(
            onClick = onClick,
            modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            ListItem(
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                leadingContent = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Router,
                            contentDescription = "Device Icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.Companion.size(40.dp)
                        )
                        Text(
                            text = device.data.baseAppName ?: "Unknown",
                            fontFamily = JetbrainsMonoFontFamily,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                },
                headlineContent = {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = device.data.readableDeviceName,
                            fontFamily = JetbrainsMonoFontFamily,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "OS: ${device.data.osName} ${device.data.osVersion}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = JetbrainsMonoFontFamily,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "${device.connection.ip}:${device.connection.port}",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = JetbrainsMonoFontFamily,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "Version: ${device.data.axerVersion}",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = JetbrainsMonoFontFamily,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                trailingContent = {
                    if (device.connection.isCustomAdded) {
                        IconButton(
                            onClick = {
                                onDelete()
                            }) {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = "Delete Device",
                            )
                        }
                    }
                })
        }
    }
}