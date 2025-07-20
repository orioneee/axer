package io.github.orioneee

import io.github.orioneee.storage.AxerSettings

class AxerConfig {
    var enableRequestMonitor: Boolean = AxerSettings.enableRequestMonitor.get()
    var enableExceptionMonitor: Boolean = AxerSettings.enableExceptionMonitor.get()
    var enableLogMonitor: Boolean = AxerSettings.enableLogMonitor.get()
    var enableDatabaseMonitor: Boolean = AxerSettings.enableDatabaseMonitor.get()
    var isRecordingLogs: Boolean = AxerSettings.isRecordingLogs.get()
    var enableRemoteMonitor: Boolean = AxerSettings.enableRemoteMonitor.get()
}
