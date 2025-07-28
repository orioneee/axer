package io.github.orioneee.domain.exceptions

import kotlinx.serialization.Serializable

@Serializable
data class SessionException(
    val exception: AxerException,
    val events: List<SessionEvent>
)