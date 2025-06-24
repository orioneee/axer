@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.oriooneee.axer

object Axer {
    val plugin = AxerPlugin

    fun recordException(
        throwable: Throwable,
    ) {
        val processor = ExceptionProcessor()
        processor.onException(throwable, false)
    }

    fun recordAsFatal(
        throwable: Throwable,
    ) {
        val processor = ExceptionProcessor()
        processor.onException(throwable, true)
    }
}

