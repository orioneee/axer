package io.github.orioneee.internal.storage

import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.PreferencesSettings

internal actual fun createSettings(name: String): ObservableSettings {
    val factory = PreferencesSettings.Factory()
    return factory.create(name)
}