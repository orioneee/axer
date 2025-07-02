@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.orioneee.processors

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import io.github.orioneee.ClearAllRequestBroadcastReceiver
import io.github.orioneee.NotificationInfo
import io.github.orioneee.axer.R
import io.github.orioneee.domain.requests.Transaction
import io.github.orioneee.koin.IsolatedContext

internal actual suspend fun updateNotification(requests: List<Transaction>) {
    val context: Context = IsolatedContext.koinApp.koin.get()
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val channel = NotificationChannel(
        NotificationInfo.CHANNEL_ID,
        NotificationInfo.CHANNEL_NAME,
        NotificationManager.IMPORTANCE_LOW
    )
    notificationManager.createNotificationChannel(channel)

    // PendingIntent to open app
    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        NotificationInfo.getLaunchIntent(context),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Clear action
    val clearIntent = Intent(context, ClearAllRequestBroadcastReceiver::class.java)
    val clearPendingIntent = PendingIntent.getBroadcast(
        context,
        11,
        clearIntent,
        PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
    )
    val action = NotificationCompat.Action(
        R.drawable.ic_exception_short_cut,
        "Clear",
        clearPendingIntent
    )

    // Compose notification content and inbox style
    val inboxStyle = NotificationCompat.InboxStyle()
    val lines = requests.map {
        val statusCode = it.responseStatus ?: "..."
        val method = it.method
        val path = it.path
        "$statusCode $method $path"
    }

    lines.forEach { inboxStyle.addLine(it) }

    // Optional: set summary text (collapsed version)
    if (lines.isNotEmpty()) {
        inboxStyle.setSummaryText("${lines.size} request${if (lines.size > 1) "s" else ""}")
    }

    val notification = NotificationCompat.Builder(context, NotificationInfo.CHANNEL_ID)
        .setContentTitle("Axer")
        .setContentText(lines.firstOrNull() ?: "No requests")
        .setStyle(inboxStyle)
        .setSmallIcon(R.drawable.ic_http)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .addAction(action)
        .setSubText(lines.size.toString())
        .build()

    notificationManager.notify(NotificationInfo.REQUEST_NOTIFICATION_ID, notification)
}
