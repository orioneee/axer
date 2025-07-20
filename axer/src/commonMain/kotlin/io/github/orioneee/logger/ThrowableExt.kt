package io.github.orioneee.logger

import io.github.orioneee.domain.requests.data.SavableError
import io.github.orioneee.domain.requests.data.SavableErrorFull

expect fun Throwable.getPlatformStackTrace(): String

internal fun Throwable.getSavableError(): SavableErrorFull {
    return SavableErrorFull(
        name = this::class.simpleName ?: "UnknownException",
        message = message ?: "No message",
        stackTrace = getPlatformStackTrace()
    )
}