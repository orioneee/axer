package io.github.orioneee.internal.logger

actual fun Throwable.getPlatformStackTrace(): String = stackTraceToString()