package io.github.orioneee.presentation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable

@Composable
actual fun MultiplatformAlertDialog(
    isShowDialog: Boolean,
    onDismiss: () -> Unit,
    title: @Composable (() -> Unit),
    confirmButton: @Composable (() -> Unit),
    content: @Composable (() -> Unit),
) {
    if (isShowDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = title,
            text = content,
            confirmButton = confirmButton,
        )
    }
}