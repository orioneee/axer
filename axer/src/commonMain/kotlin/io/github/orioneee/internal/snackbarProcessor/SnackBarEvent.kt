package io.github.orioneee.internal.snackbarProcessor

import androidx.compose.material3.SnackbarDuration
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow

internal sealed class SnackBarEvent {
    data class Message(
        val text: String,
        val duration: SnackbarDuration
    ) : SnackBarEvent()

    class Dismiss() : SnackBarEvent()
}

internal val snackbarEvents = Channel<SnackBarEvent>(
    capacity = 10,
    onBufferOverflow = BufferOverflow.DROP_OLDEST
)

internal object SnackBarController {
    fun showSnackBar(text: String, duration: SnackbarDuration = SnackbarDuration.Short) {
        snackbarEvents.trySend(SnackBarEvent.Message(text, duration))
    }

    fun dismissSnackBar() {
        snackbarEvents.trySend(SnackBarEvent.Dismiss())
    }
}