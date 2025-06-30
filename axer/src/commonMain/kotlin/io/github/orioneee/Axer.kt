@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.orioneee

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
}

internal expect fun openAxer()
internal expect fun installErrorHandler()
internal expect fun initializeIfCan()

