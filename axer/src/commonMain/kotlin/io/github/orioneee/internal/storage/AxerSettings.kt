package io.github.orioneee.internal.storage

import com.russhwolf.settings.ObservableSettings
import io.github.orioneee.AxerConfig

internal object AxerSettings {
    private val settings by lazy { createSettings("AxerSettings") }

    val enableRequestMonitor = BooleanSettingItem(settings, "enableRequestMonitor")
    val enableExceptionMonitor = BooleanSettingItem(settings, "enableExceptionMonitor")
    val enableLogMonitor = BooleanSettingItem(settings, "enableLogMonitor")
    val enableDatabaseMonitor = BooleanSettingItem(settings, "enableDatabaseMonitor")
    val isRecordingLogs = BooleanSettingItem(settings, "isRecordingLogs")

    val isSendNotification = BooleanSettingItem(settings, "isSendNotification", true)

    fun configure(config: AxerConfig) {
        enableRequestMonitor.set(config.enableRequestMonitor)
        enableExceptionMonitor.set(config.enableExceptionMonitor)
        enableLogMonitor.set(config.enableLogMonitor)
        enableDatabaseMonitor.set(config.enableDatabaseMonitor)
        isRecordingLogs.set(config.isRecordingLogs)
        isSendNotification.set(config.isSendNotification)
    }
}


internal expect fun createSettings(name: String): ObservableSettings