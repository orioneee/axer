package io.github.orioneee.storage

import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SharedPreferencesSettings
import io.github.orioneee.koin.IsolatedContext
import io.github.orioneee.koin.getContext

internal actual fun createSettings(name: String): ObservableSettings {
    val factory = SharedPreferencesSettings.Factory(IsolatedContext.getContext())
    return factory.create(name)
}