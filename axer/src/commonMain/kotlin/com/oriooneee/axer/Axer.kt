@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.oriooneee.axer

import kotlinx.coroutines.Job

object Axer {
    val plugin = AxerPlugin

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
}

internal expect fun openAxer()
internal expect fun installErrorHandler()

