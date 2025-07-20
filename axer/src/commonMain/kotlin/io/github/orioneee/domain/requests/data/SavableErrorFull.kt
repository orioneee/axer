package io.github.orioneee.domain.requests.data

import kotlinx.serialization.Serializable

@Serializable
data class SavableErrorFull(
    override val name: String,
    override val message: String,
    val stackTrace: String
) : SavableError