package io.github.orioneee.domain.other

import androidx.room.Embedded
import io.github.orioneee.domain.exceptions.AxerException
import io.github.orioneee.domain.exceptions.SessionEvent
import io.github.orioneee.domain.exceptions.SessionException
import io.github.orioneee.domain.logs.LogLine
import io.github.orioneee.domain.requests.data.TransactionShort

data class SessionEventDTO(
    val eventType: String,

    @Embedded("transaction_")
    val transactionShort: TransactionShort?,

    @Embedded("error_")
    val exception: AxerException?,

    @Embedded("log_")
    val logLine: LogLine?
) {
    companion object {
        const val EVENT_TYPE_TRANSACTION = "transaction"
        const val EVENT_TYPE_EXCEPTION = "exception"
        const val EVENT_TYPE_LOG = "log"
    }
}

fun List<SessionEventDTO>.toSessionEvents(): List<SessionEvent> {
    return mapNotNull { dto ->
        when (dto.eventType) {
            SessionEventDTO.EVENT_TYPE_TRANSACTION -> {
                listOfNotNull(
                    dto.transactionShort?.let { SessionEvent.Request(it.asRequest()) },
                    dto.transactionShort?.let {
                        it.asResponse()?.let { response -> SessionEvent.Response(response) }
                    }
                )
            }

            SessionEventDTO.EVENT_TYPE_EXCEPTION -> dto.exception?.let {
                listOf(
                    SessionEvent.Exception(
                        it
                    )
                )
            }

            SessionEventDTO.EVENT_TYPE_LOG -> {
                dto.logLine?.let { listOf(SessionEvent.Log(it)) }
            }

            else -> emptyList()
        }
    }.flatten()
}