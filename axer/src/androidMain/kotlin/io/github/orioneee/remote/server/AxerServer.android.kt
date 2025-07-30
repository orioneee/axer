package io.github.orioneee.remote.server

import android.content.Context
import io.github.orioneee.koin.IsolatedContext
import io.github.orioneee.koin.getContext

internal actual fun serverNotify(message: String) {
    val context = IsolatedContext.getContext()
    android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
}