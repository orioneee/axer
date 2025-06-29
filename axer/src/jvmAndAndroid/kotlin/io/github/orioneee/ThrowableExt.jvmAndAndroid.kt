package io.github.orioneee

actual fun Throwable.getPlatformStackTrace(): String = stackTraceToString()