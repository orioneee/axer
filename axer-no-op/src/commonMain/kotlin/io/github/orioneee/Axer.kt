@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.orioneee

import io.github.aakira.napier.LogLevel
import io.github.aakira.napier.Napier
import io.github.orioneee.processors.CleanAxerAntiLog
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
        Napier.d(tag = tag, message = message, throwable = throwable)
    }

    @OptIn(ExperimentalTime::class)
    fun e(
        tag: String? = null,
        message: String,
        throwable: Throwable? = null,
        record: Boolean = true
    ) {
        Napier.e(tag = tag, message = message, throwable = throwable)
    }

    @OptIn(ExperimentalTime::class)
    fun i(
        tag: String? = null,
        message: String,
        throwable: Throwable? = null,
        record: Boolean = true
    ) {
        Napier.i(tag = tag, message = message, throwable = throwable)
    }

    fun v(
        tag: String? = null,
        message: String,
        throwable: Throwable? = null,
        record: Boolean = true
    ) {
        Napier.v(tag = tag, message = message, throwable = throwable)
    }

    fun w(
        tag: String? = null,
        message: String,
        throwable: Throwable? = null,
        record: Boolean = true
    ) {
        Napier.w(tag = tag, message = message, throwable = throwable)
    }

    fun wtf(
        tag: String? = null,
        message: String,
        throwable: Throwable? = null,
        record: Boolean = true
    ) {
        Napier.wtf(tag = tag, message = message, throwable = throwable)
    }
}