package io.github.orioneee.logger

actual fun Throwable.getPlatformStackTrace(): String {
    return message ?: "Unknown error"
}