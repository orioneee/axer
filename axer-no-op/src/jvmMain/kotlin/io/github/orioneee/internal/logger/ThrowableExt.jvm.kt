package io.github.orioneee.internal.logger

internal actual fun Throwable.getPlatformStackTrace(): String = stackTraceToString()