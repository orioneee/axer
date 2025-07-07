package io.github.orioneee.storage

import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.PreferencesSettings

actual fun createSettings(name: String): ObservableSettings {
    val factory = PreferencesSettings.Factory()
    return factory.create(name)
}