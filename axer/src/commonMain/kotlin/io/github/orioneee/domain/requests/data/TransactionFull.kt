package io.github.orioneee.domain.requests.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.orioneee.domain.requests.Request
import io.github.orioneee.domain.requests.Response
import io.github.orioneee.domain.requests.formatters.BodyType
import kotlinx.serialization.Serializable

@Entity(tableName = "Transactions")
@Serializable
data class TransactionFull(
    @PrimaryKey(autoGenerate = true)
    override val id: Long = 0L,

    override val method: String,
    override val sendTime: Long,
    override val host: String,
    override val path: String,
    val requestBody: ByteArray? = null,
    val requestHeaders: Map<String, String> = emptyMap(),

    val responseBody: ByteArray? = null,
    override val responseTime: Long? = null,
    val responseHeaders: Map<String, String> = emptyMap(),
    override val responseStatus: Int? = null,
    @Embedded("error_")
    override val error: SavableErrorFull? = null,

    override val responseDefaultType: BodyType? = null,

    val importantInRequest: List<String> = emptyList(),
    val importantInResponse: List<String> = emptyList(),

    override val isViewed: Boolean = false,

    val size: Long = 0,


    override val sessionIdentifier: String
) : Transaction {

    fun updateToError(
        error: SavableErrorFull,
        time: Long,
    ): TransactionFull {
        return this.copy(
            error = error,
            responseTime = time,
        )
    }

    fun updateToFinished(
        responseBody: ByteArray?,
        responseTime: Long,
        responseHeaders: Map<String, String>,
        responseStatus: Int,
        bodyType: BodyType,
    ): TransactionFull {
        return this.copy(
            responseBody = responseBody,
            responseTime = responseTime,
            responseHeaders = responseHeaders,
            responseStatus = responseStatus,
            responseDefaultType = bodyType,
        )
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
            body = responseBody,
            time = responseTime!!,
            headers = responseHeaders,
            status = responseStatus!!,
            bodyType = responseDefaultType!!
        )
    }
}


