package utils

import io.github.orioneee.internal.domain.requests.data.TransactionFull
import io.github.orioneee.internal.domain.requests.formatters.BodyType
import io.github.orioneee.internal.utils.HarFile
import io.github.orioneee.internal.utils.toHarFile
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

internal object TransactionFullTestUtils {

    @OptIn(ExperimentalTime::class)
    fun createTransaction(
        method: String = "GET",
        requestBodySize: Int = 0,
        responseBodySize: Int = 0,
    ): TransactionFull {
        val requestBody =
            if (requestBodySize > 0) ByteArray(requestBodySize) { it.toByte() } else null
        val responseBody =
            if (responseBodySize > 0) ByteArray(responseBodySize) { it.toByte() } else null

        return TransactionFull(
            id = 0,
            method = method,
            sendTime = Clock.System.now().toEpochMilliseconds(),
            host = "localhost",
            path = "/test",
            requestBody = requestBody,
            requestHeaders = mapOf("Content-Type" to "application/json"),
            responseBody = responseBody,
            responseTime = Clock.System.now()
                .toEpochMilliseconds() + 1000, // Simulate 1 second response time
            responseHeaders = mapOf("Content-Type" to "application/json"),
            responseStatus = 200,
            error = null,
            responseDefaultType = BodyType.JSON,
            importantInRequest = listOf("Authorization"),
            importantInResponse = listOf("Set-Cookie"),
            isViewed = false,
            size = (requestBodySize + responseBodySize).toLong(),
            sessionIdentifier = "session-123"
        )
    }

    fun createTransactionList(
        count: Int = 2,
        method: String = "GET",
        requestBodySize: Int = 0,
        responseBodySize: Int = 0
    ): List<TransactionFull> {
        return List(count) { createTransaction(method, requestBodySize, responseBodySize) }
    }

    fun createLargeTransaction(
        requestBodySize: Int = 5_000_000,
        responseBodySize: Int = 5_000_000
    ): TransactionFull {
        return createTransaction(
            "POST",
            requestBodySize = requestBodySize,
            responseBodySize = responseBodySize
        )
    }

    fun createHarFromList(transactions: List<TransactionFull>): HarFile {
        return transactions.toHarFile()
    }
}