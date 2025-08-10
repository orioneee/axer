package io.github.orioneee.internal.domain.requests

import kotlinx.serialization.Serializable
@Serializable
data class RequestShort(
    val method: String,
    val sendTime: Long,
    val path: String,
)