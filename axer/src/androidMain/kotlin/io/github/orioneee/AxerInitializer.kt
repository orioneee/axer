package io.github.orioneee

import android.content.Context
import androidx.startup.Initializer
import io.github.orioneee.internal.koin.KoinInitializer

/**
 * Public [Initializer] entry point for Axer's `androidx.startup` setup.
 *
 * Client apps that have their own [Initializer] and want to access Axer (e.g.
 * [AxerBundledSQLiteDriver], [Axer.configure]) from inside it can declare a
 * dependency on this class:
 *
 * ```
 * override fun dependencies() = listOf(AxerInitializer::class.java)
 * ```
 *
 * This guarantees Axer's internal Koin/context wiring is ready before the
 * client initializer runs. Without an explicit dependency the order is
 * undefined and Axer may be touched before its own initializer has executed.
 */
class AxerInitializer : Initializer<Unit> {
    override fun create(context: Context) = Unit

    override fun dependencies(): List<Class<out Initializer<*>?>?> {
        return listOf(KoinInitializer::class.java)
    }
}
