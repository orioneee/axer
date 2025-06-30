package io.github.orioneee

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.rememberWindowState
import io.github.orioneee.presentation.AxerUIEntryPoint

@Composable
fun AxerWindows(
    state: WindowState = rememberWindowState(width = 800.dp, height = 600.dp),
    onCloseWindow: () -> Unit = {}
) {
    Axer.initialize()
    Window(
        state = state,
        title = "Axer Monitor",
        onCloseRequest = onCloseWindow,
    ) {
        AxerUIEntryPoint.Screen(null)
    }
}