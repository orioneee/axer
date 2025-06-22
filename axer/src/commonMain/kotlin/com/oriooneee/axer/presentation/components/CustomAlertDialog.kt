package com.oriooneee.axer.presentation.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal expect fun CustomAlertDialog(
    isShowDialog: Boolean,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
)