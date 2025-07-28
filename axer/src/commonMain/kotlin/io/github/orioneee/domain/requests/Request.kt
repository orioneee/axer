package io.github.orioneee.domain.requests

import kotlinx.serialization.Serializable

data class Request(
    val method: String,
    val sendTime: Long,
    val host: String,
    val path: String,
    val body: ByteArray?,
    val headers: Map<String, String>,
)

@Serializable
data class RequestShort(
    val method: String,
    val sendTime: Long,
    val path: String,
)