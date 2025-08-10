package io.github.orioneee

import io.github.orioneee.internal.domain.requests.formatters.BodyType

data class Response(
    val body: ByteArray?,
    val time: Long,
    val headers: Map<String, String>,
    val status: Int,
    val bodyType: BodyType,
)