package io.github.orioneee.domain.requests

import io.github.orioneee.domain.requests.formatters.BodyType

data class Response(
    val body: ByteArray?,
    val time: Long,
    val headers: Map<String, String>,
    val status: Int,
    val bodyType: BodyType,
)