package io.github.orioneee.presentation.components

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
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.orioneee.axer.generated.configs.BuildKonfig
import io.github.orioneee.axer.generated.resources.Res
import io.github.orioneee.axer.generated.resources.*
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
