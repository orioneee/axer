package io.github.orioneee

import io.github.orioneee.remote.server.getLocalIpAddress

fun Axer.installErrorHandler() {
    Thread.setDefaultUncaughtExceptionHandler(AxerUncaughtExceptionHandler())
}

actual fun installErrorHandler() {
    Axer.installErrorHandler()
}

fun Axer.getServerDetails(): String? {
    return getLocalIpAddress()
}