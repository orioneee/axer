package io.github.orioneee

actual fun installErrorHandler() {
    Thread.setDefaultUncaughtExceptionHandler(AxerUncaughtExceptionHandler())
}