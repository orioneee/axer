package io.github.orioneee.internal.domain.requests.data

import androidx.room.Embedded
import io.github.orioneee.internal.domain.requests.RequestShort
import io.github.orioneee.internal.domain.requests.ResponseShort
import io.github.orioneee.internal.domain.requests.formatters.BodyType
import kotlinx.serialization.Serializable

@Serializable
data class TransactionShort(
    override val id: Long,

    override val method: String,
    override val sendTime: Long,
    override val host: String,
    override val path: String,

    override val responseTime: Long?,
    override val responseStatus: Int?,
    @Embedded("error_")
    override val error: SavableErrorShort?,
    override val responseDefaultType: BodyType? = null,

    override val isViewed: Boolean,

    override val sessionIdentifier: String
) : Transaction {
    fun asRequest(): RequestShort {
        return RequestShort(
            method = method,
            sendTime = sendTime,
            path = path,
        )
    }

    fun asResponse(): ResponseShort? {
        return if (error?.name != null) {
            ResponseShort.Error(
                name = error.name,
                _time = responseTime ?: 0L,
                path = path,
                method = method,
            )
        } else if (isFinished()) {
            ResponseShort.Success(
                _time = responseTime ?: 0L,
                status = responseStatus ?: 0,
                path = path,
                method = method,
            )
        } else {
            null
        }
    }
}

