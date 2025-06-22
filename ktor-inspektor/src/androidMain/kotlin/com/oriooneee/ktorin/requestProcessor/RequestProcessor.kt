@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.oriooneee.ktorin.requestProcessor

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.oriooneee.KtorinActivity
import com.oriooneee.ktorin.R
import com.oriooneee.ktorin.koin.IsolatedContext
import com.oriooneee.ktorin.room.dao.RequestDao
import com.oriooneee.ktorin.domain.Transaction
import kotlin.getValue

actual suspend fun updateNotification(requests: List<Transaction>) {
    val context: Context = IsolatedContext.koinApp.koin.get()
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        val channel = android.app.NotificationChannel(
            "ktorin_channel",
            "Ktorin Notifications",
            android.app.NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)
    }
    val notificationText = requests.joinToString("\n") {
        val statusCode = it.responseStatus ?: "..."
        val method = it.method
        val path = it.path
        "$method $path - $statusCode"
    }

    val intent = Intent(context, KtorinActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notification = NotificationCompat.Builder(context, "ktorin_channel")
        .setContentTitle("Ktorin Requests")
        .setContentText(notificationText)
        .setSmallIcon(R.drawable.ic_http)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .build()

    notificationManager.notify(1, notification)
}
