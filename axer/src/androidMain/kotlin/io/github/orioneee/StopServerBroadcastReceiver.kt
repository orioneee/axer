package io.github.orioneee

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.github.orioneee.internal.NotificationInfo

internal class StopServerBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return
        Axer.stopServerIfRunning()
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.cancel(NotificationInfo.SERVER_SERVICE_NOTIFICATION_ID)
    }
}