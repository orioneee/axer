package io.github.orioneee.logger

import io.github.orioneee.domain.exceptions.SavableError

expect fun Throwable.getPlatformStackTrace(): String

internal fun Throwable.getSavableError(): SavableError {
    return SavableError(
        name = this::class.simpleName ?: "UnknownException",
        message = message ?: "No message",
        stackTrace = getPlatformStackTrace()
    )
}