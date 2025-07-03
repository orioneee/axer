package io.github.orioneee.logger

actual fun Throwable.getPlatformStackTrace(): String = stackTraceToString()