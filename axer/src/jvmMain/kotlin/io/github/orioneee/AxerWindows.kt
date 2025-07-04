package io.github.orioneee

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.rememberWindowState
import io.github.orioneee.axer.generated.resources.Res
import io.github.orioneee.axer.generated.resources.logo_circle
import io.github.orioneee.presentation.AxerUIEntryPoint
import org.jetbrains.compose.resources.painterResource

@Composable
fun AxerWindows(
    state: WindowState = rememberWindowState(width = 800.dp, height = 600.dp),
    onCloseWindow: () -> Unit = {}
) {
    val painter = painterResource(Res.drawable.logo_circle)
    Window(
        state = state,
        title = "Axer Monitor",
        icon = painter,
        onCloseRequest = onCloseWindow,
    ) {
        AxerUIEntryPoint().Screen()
    }
}