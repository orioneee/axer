package io.github.orioneee.remote.server

import androidx.compose.material3.SnackbarDuration
import io.github.orioneee.snackbarProcessor.SnackBarController

internal actual fun serverNotify(message: String) {
    SnackBarController.showSnackBar(
        message,
        SnackbarDuration.Long
    )
}