package io.github.orioneee.internal.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.alexzhirkevich.compottie.ExperimentalCompottieApi
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import io.github.orioneee.axer.generated.configs.BuildKonfig
import io.github.orioneee.axer.generated.resources.Res
import io.github.orioneee.axer.generated.resources.axer_description
import io.github.orioneee.axer.generated.resources.close
import io.github.orioneee.axer.generated.resources.github
import io.github.orioneee.axer.generated.resources.github_icon_description
import io.github.orioneee.axer.generated.resources.ic_github
import io.github.orioneee.axer.generated.resources.ic_logo
import io.github.orioneee.axer.generated.resources.ic_nest_remote
import io.github.orioneee.axer.generated.resources.logo_description
import io.github.orioneee.axer.generated.resources.report_bug
import io.github.orioneee.axer.generated.resources.server_started
import io.github.orioneee.axer.generated.resources.server_stopped_on
import io.github.orioneee.axer.generated.resources.start_server
import io.github.orioneee.axer.generated.resources.stop_server
import io.github.orioneee.axer.generated.resources.version_format
import io.github.orioneee.internal.AxerDataProvider
import io.github.orioneee.internal.domain.other.AxerServerStatus
import io.github.orioneee.internal.domain.other.Theme
import io.github.orioneee.internal.storage.AxerSettings
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AxerLogo(
    modifier: Modifier = Modifier,
) {
    Icon(
        painter = painterResource(Res.drawable.ic_logo),
        contentDescription = stringResource(Res.string.logo_description),
        tint = Color.Unspecified,
        modifier = modifier
    )
}


@Composable
fun AxerLogoDialog() {
    val currentTheme by AxerSettings.themeFlow.collectAsState(AxerSettings.theme.get())
    val isShowInfoDialog = remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    IconButton(
        onClick = { isShowInfoDialog.value = true }
    ) {
        AxerLogo()
    }

    MultiplatformAlertDialog(
        state = isShowInfoDialog,
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painterResource(Res.drawable.ic_logo),
                    contentDescription = stringResource(Res.string.logo_description),
                    tint = Color.Unspecified,
                    modifier = Modifier.size(80.dp)
                )
            }
        },
        content = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Description
                Text(
                    text = stringResource(Res.string.axer_description),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                // Theme chooser
                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(
                            8.dp,
                            Alignment.CenterHorizontally
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        listOf(
                            Theme.FOLLOW_SYSTEM to Icons.Outlined.DarkLightTheme,
                            Theme.LIGHT to Icons.Outlined.LightMode,
                            Theme.DARK to Icons.Outlined.DarkMode
                        ).forEach { (mode, icon) ->
                            ThemeOption(
                                mode = mode,
                                icon = icon,
                                selected = currentTheme == mode,
                                onClick = {
                                    AxerSettings.theme.set(mode)
                                }
                            )
                        }
                    }
                }

                HorizontalDivider(Modifier.padding(top = 4.dp))

                // Links & buttons
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(
                        8.dp,
                        Alignment.CenterHorizontally
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                ) {
                    Row(
                        modifier = Modifier
                            .height(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                uriHandler.openUri("https://github.com/orioneee/Axer")
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painterResource(Res.drawable.ic_github),
                            contentDescription = stringResource(Res.string.github_icon_description),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = stringResource(Res.string.github),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    Button(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.height(48.dp),
                        onClick = {
                            uriHandler.openUri("https://github.com/orioneee/Axer/issues")
                        }
                    ) {
                        Icon(Icons.Default.BugReport, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(Res.string.report_bug))
                    }
                }

                // Version
                Text(
                    text = stringResource(
                        Res.string.version_format,
                        BuildKonfig.VERSION_NAME
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}

@Composable
private fun ThemeOption(
    mode: Theme,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val ownColorScheme = when (mode) {
        Theme.FOLLOW_SYSTEM -> AxerTheme.systemColorScheme
        Theme.LIGHT -> AxerTheme.light
        Theme.DARK -> AxerTheme.dark
    }
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) ownColorScheme.primaryContainer
        else ownColorScheme.background,
        label = ""
    )
    val contentColor = if (selected) ownColorScheme.onPrimaryContainer
    else ownColorScheme.onBackground

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(16.dp)
            .size(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            icon,
            contentDescription = mode.name,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
    }
}


internal expect fun getServerIp(): String?
internal expect fun startServerIfCan()
internal expect fun stopServerIfCan()

private fun Color.toLottieColor(): String {
    val red = red.toString().let { if (it.endsWith(".0")) it.dropLast(2) else it }
    val green = green.toString().let { if (it.endsWith(".0")) it.dropLast(2) else it }
    val blue = blue.toString().let { if (it.endsWith(".0")) it.dropLast(2) else it }
    val alpha = alpha.toString().let { if (it.endsWith(".0")) it.dropLast(2) else it }
    return "[${red},${green},${blue},${alpha}]"
}

private suspend fun Animatable<Float, AnimationVector1D>.animateBetween(
    from: Float,
    to: Float,
    target: Float,
    duration: Int = 700
) {
    if (value != to) {
        if (value != from) {
            snapTo(from)
        }
        animateTo(
            targetValue = target,
            animationSpec = tween(duration)
        )
        snapTo(to)
    }
}


private const val pointFoStartPlay = 0.07462f
private const val pointForStartEnd = 0.537f

private val runServerAnimationRange = 0f to 49f / 130f
private val stopServerAnimationRange =
    runServerAnimationRange.second to runServerAnimationRange.first

@OptIn(ExperimentalCompottieApi::class)
@Composable
internal fun ServerStatusDialog(
    state: MutableState<Boolean>,
    serverStatus: AxerServerStatus
) {
    val pausePlayAnimationProgress =
        remember { Animatable(if (serverStatus !is AxerServerStatus.Started) pointFoStartPlay else pointForStartEnd) }
    val serverStatusAnimationProgress =
        remember { Animatable(if (serverStatus !is AxerServerStatus.Started) stopServerAnimationRange.second else runServerAnimationRange.second) }

    LaunchedEffect(serverStatus) {
        if (serverStatus !is AxerServerStatus.Started) {
            launch {
                pausePlayAnimationProgress.animateBetween(
                    from = pointForStartEnd,
                    to = pointFoStartPlay,
                    target = 1f
                )
            }
            launch {
                serverStatusAnimationProgress.animateBetween(
                    from = stopServerAnimationRange.first,
                    to = stopServerAnimationRange.second,
                    target = stopServerAnimationRange.second,
                )
            }
        } else {
            launch {
                pausePlayAnimationProgress.animateBetween(
                    from = pointFoStartPlay,
                    to = pointForStartEnd,
                    target = 0.5f
                )
            }
            launch {
                serverStatusAnimationProgress.animateBetween(
                    from = runServerAnimationRange.first,
                    to = runServerAnimationRange.second,
                    target = runServerAnimationRange.second,
                )
            }
        }
    }


    MultiplatformAlertDialog(
        state = state,
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {
                        state.value = false
                    }
                ) {
                    Text(stringResource(Res.string.close))
                }


                Button(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(48.dp),
                    onClick = {
                        if (serverStatus is AxerServerStatus.Started) {
                            stopServerIfCan()
                        } else {
                            startServerIfCan()
                        }
                    }
                ) {
                    val composition by rememberLottieComposition {
                        LottieCompositionSpec.JsonString(
                            Res.readBytes("files/pause_play.json").decodeToString()
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(
                            8.dp,
                            Alignment.CenterHorizontally
                        )
                    ) {

                        Icon(
                            painter = rememberLottiePainter(
                                composition = composition,
                                progress = { pausePlayAnimationProgress.value },
                            ),
                            modifier = Modifier.size(24.dp),
                            contentDescription = "Lottie animation"
                        )
                        AnimatedContent(serverStatus) {
                            when (it) {
                                AxerServerStatus.NotSupported -> {}
                                is AxerServerStatus.Started -> {
                                    Text(
                                        stringResource(Res.string.stop_server),
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                AxerServerStatus.Stopped -> {
                                    Text(
                                        stringResource(
                                            Res.string.start_server
                                        ),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        title = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                val primaryColor = MaterialTheme.colorScheme.primary
                val surfaceVariantColor = MaterialTheme.colorScheme.outline
                val composition by rememberLottieComposition {
                    val json = Res.readBytes("files/server_status.json")
                        .decodeToString()
                        .replace("[0,0,0,1]", primaryColor.toLottieColor())
                        .replace(
                            "[0.917999985639,0.917999985639,0.917999985639,1]",
                            surfaceVariantColor.toLottieColor()
                        )
                        .replace(
                            "[0.917647058824,0.917647058824,0.917647058824,1]",
                            surfaceVariantColor.toLottieColor()
                        )
                        .replace(
                            "[0.917647063732,0.917647063732,0.917647063732,1]",
                            surfaceVariantColor.toLottieColor()
                        )

                    LottieCompositionSpec.JsonString(json)
                }


                val painter = rememberLottiePainter(
                    composition = composition,
                    progress = { serverStatusAnimationProgress.value },
                )


                Image(
                    painter = painter,
                    contentDescription = "Server status",
                    modifier = Modifier.size(150.dp)
                )
            }
        },
        content = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (serverStatus) {
                    AxerServerStatus.NotSupported -> Text(
                        "Server not supported",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.error),
                        textAlign = TextAlign.Center
                    )

                    is AxerServerStatus.Started -> Text(
                        text = stringResource(
                            Res.string.server_started,
                            "${getServerIp()}:${serverStatus.port}"
                        ),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        textAlign = TextAlign.Center
                    )

                    AxerServerStatus.Stopped -> Text(
                        text = stringResource(
                            Res.string.server_stopped_on,
                            "${getServerIp()}:XXXXX"
                        ),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
    )
}

@Composable
internal fun ServerRunStatus(
    axerDataProvider: AxerDataProvider
) {
    val status = axerDataProvider.isServerRunning()
        .collectAsStateWithLifecycle(AxerServerStatus.NotSupported)
    if (status.value != AxerServerStatus.NotSupported) {
        val isShowServerStatusDialog = remember { mutableStateOf(false) }
        val color =
            animateColorAsState(if (status.value is AxerServerStatus.Started) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
        IconButton(
            onClick = { isShowServerStatusDialog.value = true },
            modifier = Modifier
        ) {
            Icon(
                painterResource(Res.drawable.ic_nest_remote),
                contentDescription = null,
                tint = color.value,
            )
        }

        status.value.takeIf { it is AxerServerStatus.Started || it is AxerServerStatus.Stopped }
            ?.let {
                ServerStatusDialog(
                    state = isShowServerStatusDialog,
                    serverStatus = it
                )
            }
    }
}
