package io.github.orioneee.presentation.inpsection

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.animateLottieCompositionAsState
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import io.github.orioneee.AxerUIEntryPoint
import io.github.orioneee.RemoteAxerDataProvider
import io.github.orioneee.axer.generated.resources.Res
import io.github.orioneee.axer.generated.resources.app_name_device
import io.github.orioneee.internal.domain.other.Theme
import io.github.orioneee.internal.presentation.components.AxerLogo
import io.github.orioneee.internal.presentation.components.AxerTheme.dark
import io.github.orioneee.internal.presentation.components.AxerTheme.light
import io.github.orioneee.internal.presentation.components.MultiplatformAlertDialog
import io.github.orioneee.internal.storage.AxerSettings
import io.github.orioneee.models.Device
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

class InspectionScreen {
    @Composable
    fun ConnectionLostDialog(
        isShown: Boolean,
        onDismiss: () -> Unit,
    ) {
        MultiplatformAlertDialog(
            canDismissByClickOutside = false,
            isShowDialog = isShown,
            onDismiss = onDismiss,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AxerLogo(modifier = Modifier.size(32.dp))
                    Text(
                        text = "Connection Lost",
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
                    val composition by rememberLottieComposition(
                    ) {
                        LottieCompositionSpec.JsonString(
                            io.github.orioneee.axer.debugger.generated.resources.Res.readBytes("files/loading.json")
                                .decodeToString()
                        )
                    }
                    val progress by animateLottieCompositionAsState(
                        composition,
                        iterations = Compottie.IterateForever,
                    )

//                    Icon(
//                        painterResource(Res.drawable.network_off),
//                        contentDescription = "Connection Lost",
//                        modifier = Modifier.size(48.dp),
//                        tint = MaterialTheme.colorScheme.primary
//                    )
                    Icon(
                        painter = rememberLottiePainter(
                            composition = composition,
                            progress = { progress },
                        ),
                        modifier = Modifier.size(96.dp),
                        contentDescription = "Lottie animation",
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Connection Lost",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "We're trying to reconnect. Please wait a moment.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )

                }
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = onDismiss) {
                        Text("Exit")
                    }
                }
            }
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Screen(
        navController: NavHostController,
        deviceData: Device,
    ) {
        val provider = rememberSaveable(deviceData) { RemoteAxerDataProvider(deviceData.connection.toAddress()) }
        val isConnected = provider.isConnected()
            .collectAsStateWithLifecycle(true)
        var isDialogDismissed by remember { mutableStateOf(false) }

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
                            stringResource(
                                Res.string.app_name_device,
                                deviceData.data.readableDeviceName
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                navController.popBackStack()
                            }
                        ) {
                            Icon(
                                Icons.Outlined.ArrowBackIosNew,
                                null
                            )
                        }
                    }
                )
            },
        ) {
            Box(
                modifier = Modifier.padding(top = it.calculateTopPadding()),
                contentAlignment = Alignment.Center
            ) {
                AxerUIEntryPoint().Screen(provider)
                val scope = rememberCoroutineScope()
                ConnectionLostDialog(
                    isShown = !isConnected.value && !isDialogDismissed,
                    onDismiss = {
                        scope.launch {
                            isDialogDismissed = true
                            delay(300)
                            navController.popBackStack()
                        }
                    }
                )
            }
        }
    }
}