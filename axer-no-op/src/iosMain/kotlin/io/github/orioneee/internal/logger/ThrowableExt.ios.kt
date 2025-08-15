package io.github.orioneee.internal.logger

internal actual fun Throwable.getPlatformStackTrace(): String {
    return message ?: "Unknown error"
}