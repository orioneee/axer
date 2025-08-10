package io.github.orioneee

import androidx.compose.material3.SnackbarDuration
import io.github.orioneee.internal.snackbarProcessor.SnackBarController

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