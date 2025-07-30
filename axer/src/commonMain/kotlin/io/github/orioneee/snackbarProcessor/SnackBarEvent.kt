package io.github.orioneee.snackbarProcessor

import androidx.compose.material3.SnackbarDuration
import kotlinx.coroutines.flow.MutableSharedFlow

sealed class SnackBarEvent {
    data class Message(
        val text: String,
        val duration: SnackbarDuration
    ) : SnackBarEvent()

    class Dismiss() : SnackBarEvent()
}

val snackbarEvents = MutableSharedFlow<SnackBarEvent>(
    replay = 1,
    extraBufferCapacity = 1,
)


object SnackBarController {
    fun showSnackBar(text: String, duration: SnackbarDuration = SnackbarDuration.Short) {
        snackbarEvents.tryEmit(SnackBarEvent.Message(text, duration))
    }

    fun dismissSnackBar() {
        snackbarEvents.tryEmit(SnackBarEvent.Dismiss())
    }
}