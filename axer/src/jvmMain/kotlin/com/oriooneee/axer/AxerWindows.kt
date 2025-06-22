package com.oriooneee.axer

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.rememberWindowState
import com.oriooneee.axer.koin.InitAxer
import com.oriooneee.axer.presentation.EntryPoint

@Composable
fun AxerWindows(
    state: WindowState = rememberWindowState(width = 800.dp, height = 600.dp),
    onCloseWindow: () -> Unit = {}
) {
    InitAxer.init()
    Window(
        state = state,
        title = "Axer request monitor",
        onCloseRequest = onCloseWindow,
    ) {
        EntryPoint.Screen(null)
    }
}