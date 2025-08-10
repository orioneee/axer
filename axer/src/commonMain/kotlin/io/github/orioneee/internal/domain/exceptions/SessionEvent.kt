package io.github.orioneee.internal.domain.exceptions

import io.github.orioneee.internal.domain.logs.LogLine
import io.github.orioneee.internal.domain.requests.RequestShort
import io.github.orioneee.internal.domain.requests.ResponseShort
import kotlinx.serialization.Serializable

@Serializable
sealed class SessionEvent(
    val eventTime: Long,
) {
    @Serializable
    data class Request(
        val request: RequestShort,
    ) : SessionEvent(request.sendTime)

    @Serializable
    data class Response(
        val response: ResponseShort,
    ) : SessionEvent(response.time)

    @Serializable
    data class Exception(
        val exception: AxerException
    ) : SessionEvent(exception.time)

    @Serializable
    data class Log(
        val logLine: LogLine
    ) : SessionEvent(logLine.time)
}