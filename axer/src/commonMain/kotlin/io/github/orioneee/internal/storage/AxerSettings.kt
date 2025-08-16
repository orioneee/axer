package io.github.orioneee.internal.storage

import com.russhwolf.settings.ObservableSettings
import io.github.orioneee.AxerConfig
import io.github.orioneee.internal.domain.other.Theme

object AxerSettings {
    private val settings by lazy { createSettings("AxerSettings") }

    internal val enableRequestMonitor = BooleanSettingItem(settings, "enableRequestMonitor")
    internal val enableExceptionMonitor = BooleanSettingItem(settings, "enableExceptionMonitor")
    internal val enableLogMonitor = BooleanSettingItem(settings, "enableLogMonitor")
    internal val enableDatabaseMonitor = BooleanSettingItem(settings, "enableDatabaseMonitor")
    internal val isRecordingLogs = BooleanSettingItem(settings, "isRecordingLogs")
    internal val isSendNotification = BooleanSettingItem(settings, "isSendNotification", true)

    val theme: SettingItem<Theme> = EnumSettingsItem(
        settings,
        "theme",
        Theme.FOLLOW_SYSTEM,
        Theme.serializer()
    )

    internal fun configure(config: AxerConfig) {
        enableRequestMonitor.set(config.enableRequestMonitor)
        enableExceptionMonitor.set(config.enableExceptionMonitor)
        enableLogMonitor.set(config.enableLogMonitor)
        enableDatabaseMonitor.set(config.enableDatabaseMonitor)
        isRecordingLogs.set(config.isRecordingLogs)
        isSendNotification.set(config.isSendNotification)
    }
}


internal expect fun createSettings(name: String): ObservableSettings