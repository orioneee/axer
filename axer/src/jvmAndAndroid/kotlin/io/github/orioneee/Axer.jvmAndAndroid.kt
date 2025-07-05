package io.github.orioneee

fun Axer.installErrorHandler() {
    Thread.setDefaultUncaughtExceptionHandler(AxerUncaughtExceptionHandler())
}

actual fun installErrorHandler() {
    Axer.installErrorHandler()
}