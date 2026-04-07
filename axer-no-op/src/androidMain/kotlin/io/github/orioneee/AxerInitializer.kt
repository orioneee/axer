package io.github.orioneee

import android.content.Context
import androidx.startup.Initializer

/**
 * No-op stub of the public [AxerInitializer] used by the real `axer` module.
 *
 * Exists so client code that declares
 * `override fun dependencies() = listOf(AxerInitializer::class.java)` keeps
 * compiling when swapped to `axer-no-op`. The no-op build has nothing to
 * initialize, so this initializer does nothing and has no dependencies.
 */
class AxerInitializer : Initializer<Unit> {
    override fun create(context: Context) = Unit

    override fun dependencies(): List<Class<out Initializer<*>?>?> {
        return emptyList()
    }
}
