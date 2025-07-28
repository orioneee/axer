package io.github.orioneee.domain.requests.data

import androidx.room.Ignore
import io.github.orioneee.domain.requests.formatters.BodyType

interface Transaction {
    val id: Long
    val method: String
    val sendTime: Long
    val host: String
    val path: String
    val responseTime: Long?
    val responseStatus: Int?
    val error: SavableError?
    val responseDefaultType: BodyType?
    val isViewed: Boolean
    val sessionIdentifier: String

    fun isErrorByStatusCode(): Boolean {
        return responseStatus != null && responseStatus in 400..599
    }

    fun isInProgress(): Boolean {
        return responseStatus == null && error == null
    }

    fun isFinished(): Boolean {
        return responseStatus != null
    }

    @get:Ignore
    val totalTime: Long
        get() = responseTime?.let {
            it - sendTime
        } ?: 0L

    @get:Ignore
    val fullUrl: String
        get() = "http://${host}${path}"
}