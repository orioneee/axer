package io.github.orioneee.internal.logger

import io.github.orioneee.internal.domain.requests.data.SavableErrorFull

internal expect fun Throwable.getPlatformStackTrace(): String

internal fun Throwable.getSavableError(): SavableErrorFull {
    return SavableErrorFull(
        name = this::class.simpleName ?: "UnknownException",
        message = message ?: "No message",
        stackTrace = getPlatformStackTrace()
    )
}