@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.oriooneee.ktorin.requestProcessor

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.oriooneee.KtorinActivity
import com.oriooneee.ktorin.R
import com.oriooneee.ktorin.room.dao.RequestDao
import com.oriooneee.ktorin.room.entities.Transaction
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

actual class RequestProcessor : KoinComponent {
    private val context: Context by inject()
    private val dao: RequestDao by inject()


    private suspend fun updateNotification(){
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
        val firstFive = dao.getFirstFive()
        val notificationText = firstFive.joinToString("\n") {
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

    actual suspend fun onSend(request: Transaction): Long {
        val id = dao.upsert(request)
        updateNotification()
        return id
    }

    actual suspend fun onFailed(request: Transaction) {
        dao.upsert(request)
        updateNotification()
    }

    actual suspend fun onFinished(request: Transaction) {
        dao.upsert(request)
        updateNotification()
    }
}