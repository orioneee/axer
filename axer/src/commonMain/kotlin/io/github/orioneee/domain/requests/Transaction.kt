package io.github.orioneee.domain.requests

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import io.github.orioneee.domain.exceptions.SavableError
import io.github.orioneee.domain.requests.formatters.BodyType

@Entity(tableName = "Transactions")
internal data class Transaction(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,

    val method: String,
    val sendTime: Long,
    val host: String,
    val path: String,
    val requestBody: ByteArray? = null,
    val requestHeaders: Map<String, String> = emptyMap(),

    val responseBody: ByteArray? = null,
    val responseTime: Long? = null,
    val responseHeaders: Map<String, String> = emptyMap(),
    val responseStatus: Int? = null,
    @Embedded
    val error: SavableError? = null,

    val responseDefaultType: BodyType? = null,

    val importantInRequest: List<String> = emptyList(),
    val importantInResponse: List<String> = emptyList(),

    val isViewed: Boolean = false,

    val size: Long = 0
) {

    fun isErrorByStatusCode(): Boolean {
        return responseStatus != null && responseStatus in 400..599
    }

    fun updateToError(error: SavableError): Transaction {
        return this.copy(
            error = error,
        )
    }

    fun updateToFinished(
        responseBody: ByteArray?,
        responseTime: Long,
        responseHeaders: Map<String, String>,
        responseStatus: Int,
        bodyType: BodyType,
    ): Transaction {
        return this.copy(
            responseBody = responseBody,
            responseTime = responseTime,
            responseHeaders = responseHeaders,
            responseStatus = responseStatus,
            responseDefaultType = bodyType,
        )
    }

    fun isInProgress(): Boolean {
        return responseStatus == null && error == null
    }

    fun isFinished(): Boolean {
        return responseStatus != null
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
            body = responseBody,
            time = responseTime!!,
            headers = responseHeaders,
            status = responseStatus!!,
            bodyType = responseDefaultType!!
        )
    }

    fun calculateSizeInBytes(): Long {
        val requestSize = requestBody?.size?.toLong() ?: 0L
        val responseSize = responseBody?.size?.toLong() ?: 0L
        return requestSize + responseSize + (requestHeaders.size * 100) + (responseHeaders.size * 100)
    }
}