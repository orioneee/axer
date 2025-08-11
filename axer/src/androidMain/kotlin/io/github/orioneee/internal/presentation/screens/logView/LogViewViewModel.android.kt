package io.github.orioneee.internal.presentation.screens.logView

import android.content.ClipData
import android.content.Context
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import io.github.orioneee.internal.domain.logs.LogLine


actual suspend fun copyLogs(
    clipboard: Clipboard,
    logs: List<LogLine>
) {
    val clip = ClipData.newPlainText(
        "Logs",
        logs.joinToString("\n")
    )
    clipboard.nativeClipboard.setPrimaryClip(clip)
}