@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.orioneee.requestProcessor

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
        context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        val channel = android.app.NotificationChannel(
            /* id = */ NotificationInfo.CHANNEL_ID,
            /* name = */ NotificationInfo.CHANNEL_NAME,
            /* importance = */ android.app.NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }
    val notificationText = requests.joinToString("\n") {
        val statusCode = it.responseStatus ?: "..."
        val method = it.method
        val path = it.path
        "$method $path - $statusCode"
    }


    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        NotificationInfo.getLaunchIntent(context),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val clearIntent =
        Intent(context, ClearAllRequestBroadcastReceiver::class.java)
    val clearPendingIntent =
        PendingIntent.getBroadcast(
            context,
            11,
            clearIntent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE,
        )
    val action = NotificationCompat.Action(
        R.drawable.ic_exception,
        "Clear",
        clearPendingIntent,
    )

    val notification = NotificationCompat.Builder(context, NotificationInfo.CHANNEL_ID)
        .setContentTitle("Axer")
        .setContentText(notificationText)
        .setSmallIcon(R.drawable.ic_http)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .addAction(action)
        .build()

    notificationManager.notify(NotificationInfo.REQUEST_NOTIFICATION_ID, notification)
}
