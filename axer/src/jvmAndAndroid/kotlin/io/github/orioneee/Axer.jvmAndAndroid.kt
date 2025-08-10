package io.github.orioneee

fun Axer.installErrorHandler() {
    Thread.setDefaultUncaughtExceptionHandler(AxerUncaughtExceptionHandler())
}

internal actual fun installErrorHandler() {
    Axer.installErrorHandler()
}

fun Axer.getServerDetails(): String? {
    return getLocalIpAddress()
}