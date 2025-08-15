package io.github.orioneee.internal.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material.icons.outlined.PlayCircle
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.animateLottieCompositionAsState
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
import io.github.orioneee.axer.generated.resources.server_stopped
import io.github.orioneee.axer.generated.resources.server_stopped_on
import io.github.orioneee.axer.generated.resources.start_server
import io.github.orioneee.axer.generated.resources.stop_server
import io.github.orioneee.axer.generated.resources.version_format
import io.github.orioneee.internal.AxerDataProvider
import io.github.orioneee.internal.domain.other.AxerServerStatus
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Description
                Text(
                    text = stringResource(Res.string.axer_description),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                // Links & buttons
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(
                        space = 8.dp,
                        alignment = Alignment.CenterHorizontally
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                ) {
                    // GitHub pill
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable {
                                uriHandler.openUri("https://github.com/orioneee/Axer")
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val isDark = isSystemInDarkTheme()
                        Icon(
                            painterResource(Res.drawable.ic_github),
                            contentDescription = stringResource(Res.string.github_icon_description),
                            tint = if (isDark) Color.White else Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = stringResource(Res.string.github),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    // “Report a bug” button
                    Button(
                        onClick = {
                            uriHandler.openUri("https://github.com/orioneee/Axer/issues")
                        }
                    ) {
                        Icon(Icons.Default.BugReport, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(Res.string.report_bug))
                    }
                }

                HorizontalDivider(Modifier.padding(top = 4.dp))

                // Version line with placeholder
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

internal expect fun getServerIp(): String?
internal expect fun startServerIfCan()
internal expect fun stopServerIfCan()

@Composable
internal fun ServerStatusDialog(
    state: MutableState<Boolean>,
    serverStatus: AxerServerStatus
) {
    val color =
        animateColorAsState(if (serverStatus is AxerServerStatus.Started) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
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
                val progressAnim = remember { Animatable(if (serverStatus !is AxerServerStatus.Started) 0.07462f else 0.537f) }

                LaunchedEffect(serverStatus) {
                    val target = if (serverStatus !is AxerServerStatus.Started) 1f else 0.5f
                    val start = if (serverStatus !is AxerServerStatus.Started) 0.537f else 0.07462f
                    val end = if (serverStatus !is AxerServerStatus.Started) 0.07462f else 0.537f
                    if(progressAnim.value != end){
                        if (progressAnim.value != start) {
                            progressAnim.snapTo(start)
                        }
                        progressAnim.animateTo(
                            targetValue = target,
                            animationSpec = tween(700)
                        )
                        progressAnim.snapTo(end)
                    }
                }

                Button(
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        Icon(
                            painter = rememberLottiePainter(
                                composition = composition,
                                progress = { progressAnim.value },
                            ),
                            modifier = Modifier.size(24.dp),
                            contentDescription = "Lottie animation"
                        )
                        AnimatedContent(
                            serverStatus,
                            modifier = Modifier.width(45.dp)
                        ) {
                            when (it) {
                                AxerServerStatus.NotSupported -> {}
                                is AxerServerStatus.Started -> {
                                    Text(stringResource(Res.string.stop_server))
                                }

                                AxerServerStatus.Stopped -> {
                                    Text(stringResource(Res.string.start_server))
                                }
                            }
                        }
                    }
                }
            }
        },
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painterResource(Res.drawable.ic_nest_remote),
                    contentDescription = "Server status",
                    tint = color.value,
                    modifier = Modifier.size(64.dp)
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                when (serverStatus) {
                    AxerServerStatus.NotSupported -> {}
                    is AxerServerStatus.Started -> {
                        Text(
                            text = stringResource(
                                Res.string.server_started,
                                "${getServerIp()}:${serverStatus.port}"
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }

                    AxerServerStatus.Stopped -> {
                        Text(
                            text = stringResource(
                                Res.string.server_stopped_on,
                                "${getServerIp()}:XXXXX"
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
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
            animateColorAsState(if (status.value is AxerServerStatus.Started) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
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

        status.value.takeIf { it is AxerServerStatus.Started || it is AxerServerStatus.Stopped }?.let {
            ServerStatusDialog(
                state = isShowServerStatusDialog,
                serverStatus = it
            )
        }
    }
}
