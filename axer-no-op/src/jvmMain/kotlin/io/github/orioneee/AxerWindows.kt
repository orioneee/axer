package io.github.orioneee

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.rememberWindowState

@Composable
fun AxerWindows(
    state: WindowState = rememberWindowState(width = 800.dp, height = 600.dp),
    onCloseWindow: () -> Unit = {}
) {
}


@Composable
fun ApplicationScope.AxerTrayWindow(
    state: WindowState = rememberWindowState(width = 800.dp, height = 600.dp),
    initialValue: Boolean = true,
) {
}