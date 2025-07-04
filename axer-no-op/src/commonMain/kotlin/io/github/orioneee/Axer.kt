@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.orioneee

import io.github.aakira.napier.LogLevel
import io.github.aakira.napier.Napier
import io.github.orioneee.domain.SupportedLocales
import io.github.orioneee.logger.PlatformLogger
import kotlin.time.Clock
import kotlin.time.ExperimentalTime


object Axer {
    val ktorPlugin = AxerPlugin

    fun recordException(
        throwable: Throwable,
    ) {
    }

    fun recordAsFatal(
        throwable: Throwable,
        simpleName: String = throwable::class.simpleName ?: "UnknownException",
    ) {
    }

    fun openAxerUI() {
    }

    fun installAxerErrorHandler() {
    }

    @Deprecated("No need to initialize logger")
    fun initializeLogger() {
    }

    @OptIn(ExperimentalTime::class)
    fun d(
        tag: String? = null,
        message: String,
        throwable: Throwable? = null,
        record: Boolean = true
    ) {
        PlatformLogger.performPlatformLog(
            tag = tag,
            message = message,
            throwable = throwable,
            priority = LogLevel.DEBUG,
            time = Clock.System.now().toEpochMilliseconds()
        )
    }

    @OptIn(ExperimentalTime::class)
    fun e(
        tag: String? = null,
        message: String,
        throwable: Throwable? = null,
        record: Boolean = true
    ) {
        PlatformLogger.performPlatformLog(
            tag = tag,
            message = message,
            throwable = throwable,
            priority = LogLevel.ERROR,
            time = Clock.System.now().toEpochMilliseconds()
        )
    }

    @OptIn(ExperimentalTime::class)
    fun i(
        tag: String? = null,
        message: String,
        throwable: Throwable? = null,
        record: Boolean = true
    ) {
        PlatformLogger.performPlatformLog(
            tag = tag,
            message = message,
            throwable = throwable,
            priority = LogLevel.INFO,
            time = Clock.System.now().toEpochMilliseconds()
        )
    }

    @OptIn(ExperimentalTime::class)
    fun v(
        tag: String? = null,
        message: String,
        throwable: Throwable? = null,
        record: Boolean = true
    ) {
        PlatformLogger.performPlatformLog(
            tag = tag,
            message = message,
            throwable = throwable,
            priority = LogLevel.VERBOSE,
            time = Clock.System.now().toEpochMilliseconds()
        )
    }

    @OptIn(ExperimentalTime::class)
    fun w(
        tag: String? = null,
        message: String,
        throwable: Throwable? = null,
        record: Boolean = true
    ) {
        PlatformLogger.performPlatformLog(
            tag = tag,
            message = message,
            throwable = throwable,
            priority = LogLevel.WARNING,
            time = Clock.System.now().toEpochMilliseconds()
        )
    }

    @OptIn(ExperimentalTime::class)
    fun wtf(
        tag: String? = null,
        message: String,
        throwable: Throwable? = null,
        record: Boolean = true
    ) {
        PlatformLogger.performPlatformLog(
            tag = tag,
            message = message,
            throwable = throwable,
            priority = LogLevel.ASSERT,
            time = Clock.System.now().toEpochMilliseconds()
        )
    }

    private val config = AxerConfig()

    fun configure(block: AxerConfig.() -> Unit) {
    }

    fun changeLocale(supportedLocale: SupportedLocales) {
    }
}