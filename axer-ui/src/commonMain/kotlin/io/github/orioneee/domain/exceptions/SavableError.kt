package io.github.orioneee.domain.exceptions

import kotlinx.serialization.Serializable

@Serializable
data class SavableError(
    val name: String,
    val message: String,
    val stackTrace: String
)