package io.github.orioneee.internal.koin

import org.koin.core.Koin
import org.koin.core.KoinApplication

internal object IsolatedContext {
    private var _koinApp: KoinApplication? = null

    val koinApp: KoinApplication
        get() = _koinApp ?: throw IllegalStateException(NOT_INITIALIZED_MESSAGE)

    val koin: Koin
        get() = koinApp.koin

    fun initIfNotInited(application: KoinApplication) {
        if (_koinApp == null) {
            _koinApp = application
        }
    }
}

private const val NOT_INITIALIZED_MESSAGE =
    "Axer is not initialized yet. If you call Axer (Axer.configure, Axer.installErrorHandler, " +
        "AxerBundledSQLiteDriver, AxerOkhttpInterceptor, etc.) from your own " +
        "androidx.startup.Initializer on Android, declare AxerInitializer as a dependency:\n\n" +
        "    override fun dependencies() = listOf(AxerInitializer::class.java)\n\n" +
        "Without this, the order of initializers is undefined and Axer's internal Koin context " +
        "may not be ready when you touch it."