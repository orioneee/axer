package io.github.orioneee.remote.server

import androidx.compose.material3.SnackbarDuration
import io.github.orioneee.Axer
import io.github.orioneee.snackbarProcessor.SnackBarController
import io.ktor.server.cio.CIOApplicationEngine
import io.ktor.server.engine.EmbeddedServer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal actual fun serverNotify(message: String) {
    SnackBarController.showSnackBar(
        message,
        SnackbarDuration.Long
    )
}

actual suspend fun sendNotificationAboutRunningServer(
    ip: String,
    port: Int,
    isRunning: Boolean
) {
}