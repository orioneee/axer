package io.github.orioneee.internal.presentation.screens.logView

import androidx.compose.ui.platform.Clipboard
import io.github.orioneee.internal.domain.logs.LogLine
import platform.UIKit.UIPasteboard

actual suspend fun copyLogs(
    clipboard: Clipboard,
    logs: List<LogLine>
) {
    UIPasteboard.generalPasteboard.string = logs.joinToString("\n")
}