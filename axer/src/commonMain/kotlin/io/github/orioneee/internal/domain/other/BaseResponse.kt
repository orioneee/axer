package io.github.orioneee.internal.domain.other

import kotlinx.serialization.Serializable

@Serializable
data class BaseResponse<T>(
    val status: String,
    val error: String? = null,
    val data: T? = null
) {
    fun toResult(): Result<T> {
        return if (error == null && data != null) {
            Result.success(data)
        } else {
            Result.failure(
                Exception(error ?: "Unknown error")
            )
        }
    }
}