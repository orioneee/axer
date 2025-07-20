package io.github.orioneee.domain.requests.data

import androidx.room.Embedded
import io.github.orioneee.domain.requests.formatters.BodyType
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
) : Transaction

