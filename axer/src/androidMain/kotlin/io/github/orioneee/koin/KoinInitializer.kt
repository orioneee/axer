package io.github.orioneee.koin

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import androidx.core.content.getSystemService
import androidx.startup.Initializer
import io.github.orioneee.NotificationInfo
import io.github.orioneee.axer.R
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinApplication
import org.koin.dsl.koinApplication

class KoinInitializer : Initializer<KoinApplication> {
    override fun create(context: Context): KoinApplication {
        createShortcut(context.applicationContext)
        val koinApplication = koinApplication {
            androidContext(context.applicationContext)
            modules(Modules.getModules())
        }
        IsolatedContext.initIfNotInited(koinApplication)
        return koinApplication
    }

    override fun dependencies(): List<Class<out Initializer<*>?>?> {
        return emptyList()
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
}

