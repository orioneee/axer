package io.github.orioneee

import android.content.Context
import io.github.orioneee.internal.NotificationInfo
import io.github.orioneee.internal.koin.IsolatedContext

internal actual fun openAxer() {
    val context: Context by IsolatedContext.koin.inject()
    context.startActivity(NotificationInfo.getLaunchIntent(context))
}


internal actual fun initializeIfCan() {}