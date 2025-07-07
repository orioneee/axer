package io.github.orioneee.storage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State

interface SettingItem<T : Any> {
    val key: String

    fun get(): T?
    fun set(value: T)
    fun remove()
    fun clear()

    @Composable
    fun observeAsState(): State<T>
}