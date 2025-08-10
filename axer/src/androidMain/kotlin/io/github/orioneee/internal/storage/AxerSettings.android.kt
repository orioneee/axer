package io.github.orioneee.internal.storage

import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SharedPreferencesSettings
import io.github.orioneee.internal.koin.IsolatedContext
import io.github.orioneee.internal.koin.getContext

internal actual fun createSettings(name: String): ObservableSettings {
    val factory = SharedPreferencesSettings.Factory(IsolatedContext.getContext())
    return factory.create(name)
}