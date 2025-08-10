package io.github.orioneee.internal.storage

import com.russhwolf.settings.ObservableSettings
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

internal class BooleanSettingItem(
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

    override fun asFlow(): Flow<Boolean> = callbackFlow {
        val listener = settings.addBooleanListener(key, default) { newValue ->
            trySend(newValue)
        }

        trySend(get())

        awaitClose { listener.deactivate() }
    }
}


