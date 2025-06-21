package com.oriooneee.ktorin.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable

@OptIn(markerClass = [ExperimentalMaterial3Api::class])
@Composable
actual fun CustomAlertDialog(
    isShowDialog: Boolean,
    onDismiss: () -> Unit,
    content: @Composable (() -> Unit)
) {
    if (isShowDialog) {
        BasicAlertDialog(
            onDismissRequest = {
                onDismiss()
            }
        ) {
            Box {
                content()
            }
        }
    }
}