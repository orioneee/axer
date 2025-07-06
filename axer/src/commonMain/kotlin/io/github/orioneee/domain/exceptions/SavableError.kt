package io.github.orioneee.domain.exceptions

internal data class SavableError(
    val name: String,
    val message: String,
    val stackTrace: String
)