package io.github.orioneee.internal

import android.content.Context
import android.content.Intent
import io.github.orioneee.AxerActivity

internal object NotificationInfo {
    const val REQUEST_NOTIFICATION_ID = 1
    const val EXCEPTION_NOTIFICATION_ID = 2
    const val CHANNEL_ID = "axer_notification_channel"
    const val CHANNEL_NAME = "Axer Notification Channel"
    const val SHORTCUT_ID = "axer_shortcut"
    const val SERVER_SERVICE_NOTIFICATION_ID = 50

    fun getLaunchIntent(context: Context): Intent {
        return Intent(context, AxerActivity::class.java).apply {
            setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
            )
        }
    }
}