package io.github.orioneee

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import androidx.core.content.getSystemService
import io.github.orioneee.axer.R
import io.github.orioneee.koin.IsolatedContext
import io.github.orioneee.koin.Modules
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.koinApplication


fun Axer.initialize(applicationContext: Context) {
    IsolatedContext.initIfNotInited(
        koinApplication {
            androidContext(applicationContext)
            modules(Modules.getModules())
        }
    )
    createShortcut(applicationContext)
}

internal fun createShortcut(context: Context) {
    val shortcutManager = context.getSystemService<ShortcutManager>() ?: return
    if (shortcutManager.dynamicShortcuts.any { it.id == NotificationInfo.SHORTCUT_ID }) {
        return
    }

    val shortcut =
        ShortcutInfo.Builder(context, NotificationInfo.SHORTCUT_ID)
            .setShortLabel("Open Axer")
            .setIcon(Icon.createWithResource(context, R.drawable.ic_shortcut_icon))
            .setIntent(NotificationInfo.getLaunchIntent(context).setAction(Intent.ACTION_VIEW))
            .build()
    try {
        shortcutManager.addDynamicShortcuts(listOf(shortcut))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}