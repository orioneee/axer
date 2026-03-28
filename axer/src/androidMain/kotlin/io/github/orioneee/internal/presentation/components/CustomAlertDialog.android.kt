package io.github.orioneee.internal.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

@Composable
actual fun MultiplatformAlertDialog(
    canDismissByClickOutside: Boolean,
    isShowDialog: Boolean,
    onDismiss: () -> Unit,
    title: @Composable (() -> Unit),
    cancelButton: @Composable (() -> Unit)?,
    confirmButton: @Composable (() -> Unit),
    content: @Composable (() -> Unit),
) {
    if (isShowDialog) {
        AlertDialog(
            properties = DialogProperties(
                dismissOnClickOutside = canDismissByClickOutside
            ),
            onDismissRequest = onDismiss,
            title = title,
            text = content,
            dismissButton = cancelButton,
            confirmButton = confirmButton,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 0.dp,
        )
    }
}
