package com.oriooneee.axer

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.rememberWindowState


@Composable
fun AxerWindows(
    state: WindowState = rememberWindowState(width = 800.dp, height = 600.dp),
    onCloseWindow: () -> Unit = {}
) {
}