package io.github.orioneee

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.github.orioneee.koin.IsolatedContext
import io.github.orioneee.room.dao.RequestDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ClearAllRequestBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return
        Axer.initialize(context.applicationContext)
        val dao: RequestDao by IsolatedContext.koin.inject()
        CoroutineScope(Dispatchers.IO).launch {
            dao.deleteAll()
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.cancel(NotificationInfo.REQUEST_NOTIFICATION_ID)
        }
    }
}