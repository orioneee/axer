package io.github.orioneee.internal.logger

actual fun Throwable.getPlatformStackTrace(): String {
    return message ?: "Unknown error"
}