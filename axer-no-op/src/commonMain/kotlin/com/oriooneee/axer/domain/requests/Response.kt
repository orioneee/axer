package com.oriooneee.axer.domain.requests

data class Response(
    val responseBody: String?,
    val responseTime: Long,
    val responseHeaders: Map<String, String>,
    val responseStatus: Int,
    val imageBytes: ByteArray?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Response

        if (responseTime != other.responseTime) return false
        if (responseStatus != other.responseStatus) return false
        if (responseBody != other.responseBody) return false
        if (responseHeaders != other.responseHeaders) return false
        if (!imageBytes.contentEquals(other.imageBytes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = responseTime.hashCode()
        result = 31 * result + responseStatus
        result = 31 * result + (responseBody?.hashCode() ?: 0)
        result = 31 * result + responseHeaders.hashCode()
        result = 31 * result + (imageBytes?.contentHashCode() ?: 0)
        return result
    }
}