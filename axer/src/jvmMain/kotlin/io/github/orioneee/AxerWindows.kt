package io.github.orioneee

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.MenuScope
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.rememberWindowState
import io.github.orioneee.axer.generated.resources.Res
import io.github.orioneee.axer.generated.resources.ic_logo
import io.github.orioneee.axer.generated.resources.logo_circle
import io.github.orioneee.axer.generated.resources.open_axer
import io.github.orioneee.koin.IsolatedContext
import io.github.orioneee.presentation.AxerUIEntryPoint
import io.github.orioneee.room.AxerDatabase
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.KoinIsolatedContext
import org.koin.compose.koinInject

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
        KoinIsolatedContext(
            IsolatedContext.koinApp
        ) {
            val database: AxerDatabase = koinInject()
            AxerUIEntryPoint().Screen(RoomAxerDataProvider(database))
        }
    }
}

@Composable
fun MenuScope.AxerTrayIcon(
    onClick: () -> Unit,
) {
    Item(
        text = stringResource(Res.string.open_axer),
        onClick = onClick,
    )
}

@Composable
fun ApplicationScope.AxerTrayWindow(
    state: WindowState = rememberWindowState(width = 800.dp, height = 600.dp),
    initialValue: Boolean = true,
) {
    var showAxer by rememberSaveable { mutableStateOf(initialValue) }

    Tray(
        icon = painterResource(Res.drawable.ic_logo),
        menu = {
            AxerTrayIcon { showAxer = true }
        }
    )

    if (showAxer) {
        AxerWindows(
            state = state,
            onCloseWindow = { showAxer = false },
        )
    }
}