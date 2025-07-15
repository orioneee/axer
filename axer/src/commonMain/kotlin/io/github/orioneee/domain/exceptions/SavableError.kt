package io.github.orioneee.domain.exceptions

import kotlinx.serialization.Serializable

@Serializable
internal data class SavableError(
    val name: String,
    val message: String,
    val stackTrace: String
)