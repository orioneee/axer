package io.github.orioneee.internal.storage

import com.russhwolf.settings.ObservableSettings
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

internal class EnumSettingsItem<T : Any>(
    private val settings: ObservableSettings,
    override val key: String,
    private val default: T,
    private val serializer: KSerializer<T>
) : SettingItem<T> {

    private val json = Json { ignoreUnknownKeys = true }

    override fun get(): T {
        val stored = settings.getString(key, json.encodeToString(serializer, default))
        return json.decodeFromString(serializer, stored)
    }

    override fun set(value: T) {
        settings.putString(key, json.encodeToString(serializer, value))
    }

    override fun remove() {
        settings.remove(key)
    }

    override fun clear() {
        settings.clear()
    }

    private var lastValue: T? = null

    override fun asFlow(): Flow<T> = callbackFlow {
        lastValue?.let {
            trySend(it)
        } ?: run {
            trySend(get())
            lastValue = get()
        }
        val listener = settings.addStringListener(
            key,
            json.encodeToString(serializer, default)
        ) { newValue ->
            lastValue = json.decodeFromString(serializer, newValue)
            trySend(json.decodeFromString(serializer, newValue))
        }

        trySend(get())

        awaitClose { listener.deactivate() }
    }
}

