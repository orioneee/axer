package io.github.orioneee.presentation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties

@Composable
actual fun MultiplatformAlertDialog(
    canDismissByClickOutside: Boolean,
    isShowDialog: Boolean,
    onDismiss: () -> Unit,
    title: @Composable (() -> Unit),
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
            confirmButton = confirmButton,
        )
    }
}