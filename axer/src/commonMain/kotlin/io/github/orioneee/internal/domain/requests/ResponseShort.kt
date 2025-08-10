package io.github.orioneee.internal.domain.requests

import kotlinx.serialization.Serializable


@Serializable
sealed class ResponseShort(
    val time: Long,
) {
    @Serializable
    data class Success(
        val _time: Long,
        val method: String,
        val status: Int,
        val path: String,
    ): ResponseShort(_time){
        fun isErrorByStatusCode(): Boolean {
            return status in 400..599
        }
    }
    @Serializable
    data class Error(
        val _time: Long,
        val path: String,
        val method: String,
        val name: String,
    ): ResponseShort(_time)
}