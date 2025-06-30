package io.github.orioneee.domain.requests

data class Response(
    val body: String?,
    val time: Long,
    val headers: Map<String, String>,
    val status: Int,
    val image: ByteArray?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Response

        if (time != other.time) return false
        if (status != other.status) return false
        if (body != other.body) return false
        if (headers != other.headers) return false
        if (!image.contentEquals(other.image)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = time.hashCode()
        result = 31 * result + status
        result = 31 * result + (body?.hashCode() ?: 0)
        result = 31 * result + headers.hashCode()
        result = 31 * result + (image?.contentHashCode() ?: 0)
        return result
    }
}