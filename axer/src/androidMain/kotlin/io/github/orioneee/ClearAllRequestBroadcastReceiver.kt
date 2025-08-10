package io.github.orioneee

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.github.orioneee.internal.NotificationInfo
import io.github.orioneee.internal.koin.IsolatedContext
import io.github.orioneee.internal.room.dao.RequestDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class ClearAllRequestBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return
        val dao: RequestDao by IsolatedContext.koin.inject()
        CoroutineScope(Dispatchers.IO).launch {
            cancelNotification(context)
            dao.deleteAll()
        }
    }
}

internal fun cancelNotification(context: Context) {
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
    notificationManager.cancel(NotificationInfo.REQUEST_NOTIFICATION_ID)
}