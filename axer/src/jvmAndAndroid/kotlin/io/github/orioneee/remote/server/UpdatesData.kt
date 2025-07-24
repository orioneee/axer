package io.github.orioneee.remote.server

import kotlinx.serialization.Serializable

@Serializable
data class UpdatesData<T>(
    val updatedOrCreated: List<T>,
    val deleted: List<Long>,
    val replaceWith: List<T>
)