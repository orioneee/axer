package io.github.orioneee.internal.presentation.screens.requests.list

import io.github.orioneee.cancelNotification
import io.github.orioneee.internal.koin.IsolatedContext
import io.github.orioneee.internal.koin.getContext

actual fun onClearAllRequests() {
    cancelNotification(IsolatedContext.getContext())
}