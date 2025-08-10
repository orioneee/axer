package io.github.orioneee.internal.snackbarProcessor

import androidx.compose.material3.SnackbarDuration
import kotlinx.coroutines.flow.MutableSharedFlow

internal sealed class SnackBarEvent {
    data class Message(
        val text: String,
        val duration: SnackbarDuration
    ) : SnackBarEvent()

    class Dismiss() : SnackBarEvent()
}

internal val snackbarEvents = MutableSharedFlow<SnackBarEvent>(
    replay = 1,
    extraBufferCapacity = 1,
)


internal object SnackBarController {
    fun showSnackBar(text: String, duration: SnackbarDuration = SnackbarDuration.Short) {
        snackbarEvents.tryEmit(SnackBarEvent.Message(text, duration))
    }

    fun dismissSnackBar() {
        snackbarEvents.tryEmit(SnackBarEvent.Dismiss())
    }
}