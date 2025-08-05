@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.orioneee

import io.github.aakira.napier.LogLevel
import io.github.orioneee.processors.LogProcessor
import io.github.orioneee.logger.PlatformLogger
import io.github.orioneee.logger.performPlatformLog
import io.github.orioneee.processors.ExceptionProcessor
import io.github.orioneee.storage.AxerSettings
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

object Axer {
    val ktorPlugin = AxerPlugin

    fun recordException(
        throwable: Throwable,
        onRecorded: () -> Unit = {}
    ) {
        val processor = ExceptionProcessor()
        processor.onException(throwable, false, onRecorded)
    }

    fun recordAsFatal(
        throwable: Throwable,
        onRecorded: () -> Unit = {}
    ) {
        val processor = ExceptionProcessor()
        processor.onException(throwable, true, onRecorded)
    }

    fun openAxerUI() {
        openAxer()
    }

    @Deprecated("Use Axer.installErrorHandler(), in your platform module")
    fun installAxerErrorHandler() {
        installErrorHandler()
    }
    internal fun initIfCan() {
        initializeIfCan()
    }
    init {
        initIfCan()
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
        val time = Clock.System.now().toEpochMilliseconds()
        PlatformLogger.performPlatformLog(
            tag = tag,
            message = message,
            throwable = throwable,
            priority = LogLevel.DEBUG,
            time = time
        )
        if (record) {
            LogProcessor.onLog(
                tag = tag,
                message = message,
                level = LogLevel.DEBUG,
                throwable = throwable,
                time = time
            )
        }

    }

    @OptIn(ExperimentalTime::class)
    fun e(
        tag: String? = null,
        message: String,
        throwable: Throwable? = null,
        record: Boolean = true
    ) {
        val time = Clock.System.now().toEpochMilliseconds()
        PlatformLogger.performPlatformLog(
            tag = tag,
            message = message,
            throwable = throwable,
            priority = LogLevel.ERROR,
            time = time
        )
        if (record) {
            LogProcessor.onLog(
                tag = tag,
                message = message,
                level = LogLevel.ERROR,
                throwable = throwable,
                time = time
            )
        }

    }

    @OptIn(ExperimentalTime::class)
    fun i(
        tag: String? = null,
        message: String,
        throwable: Throwable? = null,
        record: Boolean = true
    ) {
        val time = Clock.System.now().toEpochMilliseconds()
        PlatformLogger.performPlatformLog(
            tag = tag,
            message = message,
            throwable = throwable,
            priority = LogLevel.INFO,
            time = time
        )
        if (record) {
            LogProcessor.onLog(
                tag = tag,
                message = message,
                level = LogLevel.INFO,
                throwable = throwable,
                time = time
            )
        }

    }

    @OptIn(ExperimentalTime::class)
    fun v(
        tag: String? = null,
        message: String,
        throwable: Throwable? = null,
        record: Boolean = true
    ) {
        val time = Clock.System.now().toEpochMilliseconds()
        PlatformLogger.performPlatformLog(
            tag = tag,
            message = message,
            throwable = throwable,
            priority = LogLevel.VERBOSE,
            time = time
        )
        if (record) {
            LogProcessor.onLog(
                tag = tag,
                message = message,
                level = LogLevel.VERBOSE,
                throwable = throwable,
                time = time
            )
        }
    }

    @OptIn(ExperimentalTime::class)
    fun w(
        tag: String? = null,
        message: String,
        throwable: Throwable? = null,
        record: Boolean = true
    ) {
        val time = Clock.System.now().toEpochMilliseconds()
        PlatformLogger.performPlatformLog(
            tag = tag,
            message = message,
            throwable = throwable,
            priority = LogLevel.WARNING,
            time = time
        )
        if (record) {
            LogProcessor.onLog(
                tag = tag,
                message = message,
                level = LogLevel.WARNING,
                throwable = throwable,
                time = time
            )
        }

    }

    @OptIn(ExperimentalTime::class)
    fun wtf(
        tag: String? = null,
        message: String,
        throwable: Throwable? = null,
        record: Boolean = true
    ) {
        val time = Clock.System.now().toEpochMilliseconds()
        PlatformLogger.performPlatformLog(
            tag = tag,
            message = message,
            throwable = throwable,
            priority = LogLevel.ASSERT,
            time = time
        )
        if (record) {
            LogProcessor.onLog(
                tag = tag,
                message = message,
                level = LogLevel.ASSERT,
                throwable = throwable,
                time = time
            )
        }
    }

    private val config = AxerConfig()

    fun configure(block: AxerConfig.() -> Unit) {
        config.block()
        AxerSettings.configure(config)
    }

    fun getConfig(): AxerConfig {
        return config
    }
}

internal expect fun openAxer()
internal expect fun installErrorHandler()
internal expect fun initializeIfCan()