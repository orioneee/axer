package io.github.orioneee.remote.server

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import io.github.orioneee.koin.IsolatedContext
import io.github.orioneee.koin.getContext

internal actual fun serverNotify(message: String) {
    val context = IsolatedContext.getContext()
    Handler(Looper.getMainLooper()).post {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}