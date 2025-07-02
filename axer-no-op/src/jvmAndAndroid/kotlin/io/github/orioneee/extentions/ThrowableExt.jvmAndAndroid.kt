package io.github.orioneee.extentions

actual fun Throwable.getPlatformStackTrace(): String = stackTraceToString()
