package io.github.orioneee.storage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SettingsListener

class BooleanSettingItem(
    private val settings: ObservableSettings,
    override val key: String,
    private val default: Boolean = true
) : SettingItem<Boolean> {

    override fun get(): Boolean = settings.getBoolean(key, default)

    override fun set(value: Boolean) {
        settings.putBoolean(key, value)
    }

    override fun remove() {
        settings.remove(key)
    }

    override fun clear() {
        settings.clear()
    }

    @Composable
    override fun observeAsState(): State<Boolean> {
        val state = remember { mutableStateOf(get()) }

        DisposableEffect(key) {
            val listener: SettingsListener = settings.addBooleanListener(key, default) { newValue ->
                state.value = newValue
            }

            onDispose {
                listener.deactivate()
            }
        }

        return state
    }
}

