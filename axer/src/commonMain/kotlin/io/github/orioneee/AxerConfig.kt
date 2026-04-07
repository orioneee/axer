package io.github.orioneee

import io.github.orioneee.internal.storage.AxerSettings

class AxerConfig {
    var enableRequestMonitor: Boolean
        get() = AxerSettings.enableRequestMonitor.get()
        set(value) { AxerSettings.enableRequestMonitor.set(value) }

    var enableExceptionMonitor: Boolean
        get() = AxerSettings.enableExceptionMonitor.get()
        set(value) { AxerSettings.enableExceptionMonitor.set(value) }

    var enableLogMonitor: Boolean
        get() = AxerSettings.enableLogMonitor.get()
        set(value) { AxerSettings.enableLogMonitor.set(value) }

    var enableDatabaseMonitor: Boolean
        get() = AxerSettings.enableDatabaseMonitor.get()
        set(value) { AxerSettings.enableDatabaseMonitor.set(value) }

    var isRecordingLogs: Boolean
        get() = AxerSettings.isRecordingLogs.get()
        set(value) { AxerSettings.isRecordingLogs.set(value) }

    var isSendNotification: Boolean
        get() = AxerSettings.isSendNotification.get()
        set(value) { AxerSettings.isSendNotification.set(value) }
}
