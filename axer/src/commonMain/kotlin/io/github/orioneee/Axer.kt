@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.orioneee

import io.github.aakira.napier.LogLevel
import io.github.aakira.napier.Napier
import io.github.orioneee.domain.SupportedLocales
import io.github.orioneee.domain.logs.LogProcessor
import io.github.orioneee.logger.PlatformLogger
import io.github.orioneee.presentation.AxerUIEntryPoint
import io.github.orioneee.processors.ExceptionProcessor
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

object Axer {
    val ktorPlugin = AxerPlugin

    fun recordException(
        throwable: Throwable,
    ) {
        val processor = ExceptionProcessor()
        processor.onException(throwable, throwable::class.simpleName ?: "UnknownException", false)
    }

    fun recordAsFatal(
        throwable: Throwable,
        simpleName: String = throwable::class.simpleName ?: "UnknownException",
    ) {
        val processor = ExceptionProcessor()
        processor.onException(throwable, simpleName, true)
    }

    fun openAxerUI() {
        openAxer()
    }

    fun installAxerErrorHandler() {
        installErrorHandler()
    }

    internal fun initIfCan() {
        initializeIfCan()
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
        if (record) {
            val time = Clock.System.now().toEpochMilliseconds()
            val processor = LogProcessor()
            processor.onLog(
                tag = tag,
                message = message,
                level = LogLevel.DEBUG,
                throwable = throwable,
                time = time
            )
        }
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
        if (record) {
            val time = Clock.System.now().toEpochMilliseconds()
            val processor = LogProcessor()
            processor.onLog(
                tag = tag,
                message = message,
                level = LogLevel.ERROR,
                throwable = throwable,
                time = time
            )
        }
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
        if (record) {
            val time = Clock.System.now().toEpochMilliseconds()
            val processor = LogProcessor()
            processor.onLog(
                tag = tag,
                message = message,
                level = LogLevel.INFO,
                throwable = throwable,
                time = time
            )
        }
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
        if (record) {
            val time = Clock.System.now().toEpochMilliseconds()
            val processor = LogProcessor()
            processor.onLog(
                tag = tag,
                message = message,
                level = LogLevel.VERBOSE,
                throwable = throwable,
                time = time
            )
        }
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
        if (record) {
            val processor = LogProcessor()
            processor.onLog(
                tag = tag,
                message = message,
                level = LogLevel.WARNING,
                throwable = throwable,
                time = Clock.System.now().toEpochMilliseconds()
            )
        }
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
        if (record) {
            val time = Clock.System.now().toEpochMilliseconds()
            val processor = LogProcessor()
            processor.onLog(
                tag = tag,
                message = message,
                level = LogLevel.ASSERT,
                throwable = throwable,
                time = time
            )
        }
        PlatformLogger.performPlatformLog(
            tag = tag,
            message = message,
            throwable = throwable,
            priority = LogLevel.ASSERT,
            time = Clock.System.now().toEpochMilliseconds()
        )
    }

    fun changeLocale(supportedLocale: SupportedLocales) {
        AxerUIEntryPoint.changeLocale(supportedLocale)
    }
}

internal expect fun openAxer()
internal expect fun installErrorHandler()
internal expect fun initializeIfCan()

