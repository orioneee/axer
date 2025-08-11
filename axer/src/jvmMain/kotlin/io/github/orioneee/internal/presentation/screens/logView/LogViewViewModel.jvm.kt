package io.github.orioneee.internal.presentation.screens.logView

import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import io.github.orioneee.axer.generated.resources.Res
import io.github.orioneee.axer.generated.resources.logs_copied
import io.github.orioneee.axer.generated.resources.oops_something_went_wrong
import io.github.orioneee.internal.domain.logs.LogLine
import io.github.orioneee.internal.snackbarProcessor.SnackBarController
import org.jetbrains.compose.resources.getString
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

actual suspend fun copyLogs(
    clipboard: Clipboard,
    logs: List<LogLine>
) {
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    clipboard.setContents(StringSelection(logs.joinToString("\n")), null)
}