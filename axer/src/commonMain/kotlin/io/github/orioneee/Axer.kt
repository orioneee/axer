@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.orioneee

import io.github.aakira.napier.LogLevel
import io.github.aakira.napier.Napier
import io.github.orioneee.domain.logs.LogProcessor
import io.github.orioneee.processors.AxerLogSaver
import io.github.orioneee.processors.CleanAxerAntiLog
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

    fun openAxerUI(){
        openAxer()
    }

    fun installAxerErrorHandler(){
        installErrorHandler()
    }

    internal fun initIfCan(){
        initializeIfCan()
    }

    fun initializeLogger() {
        Napier.base(CleanAxerAntiLog())
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
        Napier.d(tag = tag, message = message, throwable = throwable)
    }

    @OptIn(ExperimentalTime::class)
    fun e(tag: String? = null, message: String, throwable: Throwable? = null, record: Boolean = true) {
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
        Napier.e(tag = tag, message = message, throwable = throwable)
    }

    @OptIn(ExperimentalTime::class)
    fun i(tag: String? = null, message: String, throwable: Throwable? = null, record: Boolean = true) {
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
        Napier.i(tag = tag, message = message, throwable = throwable)
    }

    @OptIn(ExperimentalTime::class)
    fun v(tag: String? = null, message: String, throwable: Throwable? = null, record: Boolean = true) {
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
        Napier.v(tag = tag, message = message, throwable = throwable)
    }

    @OptIn(ExperimentalTime::class)
    fun w(tag: String? = null, message: String, throwable: Throwable? = null, record: Boolean = true) {
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
        Napier.w(tag = tag, message = message, throwable = throwable)
    }

    @OptIn(ExperimentalTime::class)
    fun wtf(tag: String? = null, message: String, throwable: Throwable? = null, record: Boolean = true) {
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
        Napier.wtf(tag = tag, message = message, throwable = throwable)
    }
}

internal expect fun openAxer()
internal expect fun installErrorHandler()
internal expect fun initializeIfCan()

