package com.oriooneee.axer

actual object Axer {
    val plugin = AxerPlugin

    val exceptionHandler = AxerUncaughtExceptionHandler()

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