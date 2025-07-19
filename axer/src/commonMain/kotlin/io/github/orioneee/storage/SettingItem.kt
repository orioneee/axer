package io.github.orioneee.storage

import kotlinx.coroutines.flow.Flow

interface SettingItem<T : Any> {
    val key: String

    fun get(): T
    fun set(value: T)
    fun remove()
    fun clear()

    fun asFlow(): Flow<T>
}