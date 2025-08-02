package io.github.orioneee.remote.server

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import io.github.orioneee.ClearAllRequestBroadcastReceiver
import io.github.orioneee.NotificationInfo
import io.github.orioneee.StopServerBroadcastReceiver
import io.github.orioneee.axer.R
import io.github.orioneee.axer.generated.resources.Res
import io.github.orioneee.axer.generated.resources.axer_server
import io.github.orioneee.axer.generated.resources.cancel
import io.github.orioneee.axer.generated.resources.clear
import io.github.orioneee.axer.generated.resources.server_started
import io.github.orioneee.axer.generated.resources.stop
import io.github.orioneee.koin.IsolatedContext
import io.github.orioneee.koin.getContext
import org.jetbrains.compose.resources.getString

internal actual fun serverNotify(message: String) {
    val context = IsolatedContext.getContext()
    Handler(Looper.getMainLooper()).post {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}

actual suspend fun sendNotificationAboutRunningServer(
    ip: String,
    port: Int,
    isRunning: Boolean
) {
    val context = IsolatedContext.getContext()
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            NotificationInfo.CHANNEL_ID,
            NotificationInfo.CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }
    if (isRunning) {
        val startedMsg = getString(Res.string.server_started, "$ip:$port")
        Log.d("AxerServer", "Server started: $startedMsg")

        val clearIntent = Intent(context, StopServerBroadcastReceiver::class.java)
        val clearPendingIntent = PendingIntent.getBroadcast(
            context,
            12,
            clearIntent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val action = NotificationCompat.Action(
            R.drawable.ic_exception_short_cut,
            getString(Res.string.stop),
            clearPendingIntent
        )

        val notification = NotificationCompat.Builder(context, NotificationInfo.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_server)
            .setContentTitle(getString(Res.string.axer_server))
            .setContentText(startedMsg)
            .addAction(action)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NotificationInfo.SERVER_SERVICE_NOTIFICATION_ID, notification)

    } else{
        notificationManager.cancel(NotificationInfo.SERVER_SERVICE_NOTIFICATION_ID)
    }
}