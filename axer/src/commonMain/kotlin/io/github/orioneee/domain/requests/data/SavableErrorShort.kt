package io.github.orioneee.domain.requests.data

import kotlinx.serialization.Serializable

@Serializable
data class SavableErrorShort(
    override val name: String,
    override val message: String,
) : SavableError

