package io.github.orioneee.internal.domain.exceptions

import kotlinx.serialization.Serializable

@Serializable
data class SessionException(
    val exception: AxerException,
    val events: List<SessionEvent>
)