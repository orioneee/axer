package io.github.orioneee

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import io.github.AxerActivity
import io.github.orioneee.domain.exceptions.AxerException
import io.github.orioneee.koin.IsolatedContext

internal actual fun notifyAboutException(exception: AxerException) {
    val context: Context by IsolatedContext.koin.inject()

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
    val notificationText = exception.shortName

    val intent = Intent(context, AxerActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notification = NotificationCompat.Builder(context, NotificationInfo.CHANNEL_ID)
        .setContentTitle("Recorded new exception")
        .setContentText(notificationText)
        .setSmallIcon(R.drawable.ic_exception)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .build()

    notificationManager.notify(exception.id.toInt(), notification)
}