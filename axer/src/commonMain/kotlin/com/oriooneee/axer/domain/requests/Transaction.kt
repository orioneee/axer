package com.oriooneee.axer.domain.requests

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "Transactions")
internal data class Transaction(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,

    val method: String,
    val sendTime: Long,
    val host: String,
    val path: String,
    val requestBody: String? = null,
    val requestHeaders: Map<String, String> = emptyMap(),

    val responseBody: String? = null,
    val responseTime: Long? = null,
    val responseHeaders: Map<String, String> = emptyMap(),
    val responseStatus: Int? = null,
    val imageBytes: ByteArray? = null,

    val error: String? = null,

    val isImage: Boolean? = null,

    val importantInRequest: List<String> = emptyList(),
    val importantInResponse: List<String> = emptyList(),

    val isViewed: Boolean = false
) {

    fun updateToError(error: String): Transaction {
        return this.copy(
            error = error,
        )
    }

    fun updateToFinished(
        responseBody: String?,
        imageBytes: ByteArray?,
        responseTime: Long,
        responseHeaders: Map<String, String>,
        responseStatus: Int,
        isImage: Boolean
    ): Transaction {
        return this.copy(
            responseBody = responseBody,
            responseTime = responseTime,
            responseHeaders = responseHeaders,
            responseStatus = responseStatus,
            imageBytes = imageBytes,
            isImage = isImage
        )
    }

    fun isInProgress(): Boolean {
        return responseStatus == null && error == null
    }

    fun isFinished(): Boolean {
        return responseStatus != null || error == null
    }

    @get:Ignore
    val totalTime: Long
        get() = if (responseTime != null) {
            responseTime - sendTime
        } else {
            0L
        }

    @get:Ignore
    val fullUrl: String
        get() = "http://${host}${path}"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Transaction

        if (id != other.id) return false
        if (sendTime != other.sendTime) return false
        if (responseTime != other.responseTime) return false
        if (responseStatus != other.responseStatus) return false
        if (method != other.method) return false
        if (host != other.host) return false
        if (path != other.path) return false
        if (requestBody != other.requestBody) return false
        if (requestHeaders != other.requestHeaders) return false
        if (responseBody != other.responseBody) return false
        if (responseHeaders != other.responseHeaders) return false
        if (!imageBytes.contentEquals(other.imageBytes)) return false
        if (error != other.error) return false
        if (totalTime != other.totalTime) return false
        if (fullUrl != other.fullUrl) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + sendTime.hashCode()
        result = 31 * result + (responseTime?.hashCode() ?: 0)
        result = 31 * result + (responseStatus ?: 0)
        result = 31 * result + method.hashCode()
        result = 31 * result + host.hashCode()
        result = 31 * result + path.hashCode()
        result = 31 * result + (requestBody?.hashCode() ?: 0)
        result = 31 * result + requestHeaders.hashCode()
        result = 31 * result + (responseBody?.hashCode() ?: 0)
        result = 31 * result + responseHeaders.hashCode()
        result = 31 * result + (imageBytes?.contentHashCode() ?: 0)
        result = 31 * result + (error?.hashCode() ?: 0)
        result = 31 * result + totalTime.hashCode()
        result = 31 * result + fullUrl.hashCode()
        return result
    }

    fun asRequest(): Request {
        return Request(
            method = method,
            sendTime = sendTime,
            host = host,
            path = path,
            body = requestBody,
            headers = requestHeaders
        )
    }

    fun asResponse(): Response {
        return Response(
            responseBody = responseBody,
            responseTime = responseTime!!,
            responseHeaders = responseHeaders,
            responseStatus = responseStatus!!,
            imageBytes = imageBytes
        )
    }
}