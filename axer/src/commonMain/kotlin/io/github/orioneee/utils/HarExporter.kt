package io.github.orioneee.utils

import io.github.orioneee.Axer
import io.github.orioneee.axer.generated.configs.BuildKonfig
import io.github.orioneee.domain.requests.data.Transaction
import io.github.orioneee.domain.requests.data.TransactionFull
import io.github.orioneee.domain.requests.formatters.BodyType
import io.ktor.util.encodeBase64
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
data class HarFile(
    val log: HarLog
)

@Serializable
data class HarLog(
    val version: String = "1.2",
    val creator: HarCreator = HarCreator(),
    val entries: List<HarEntry>
)

@Serializable
data class HarCreator(
    val name: String = "Axer",
    val version: String = BuildKonfig.VERSION_NAME
)

@Serializable
data class HarEntry(
    val startedDateTime: String,
    val time: Long,
    val request: HarRequest,
    val response: HarResponse,
    val cache: JsonObject = JsonObject(emptyMap()),
    val timings: HarTimings
)

@Serializable
data class HarRequest(
    val method: String,
    val url: String,
    val httpVersion: String = "HTTP/1.1",
    val headers: List<HarHeader>,
    val queryString: List<HarQueryParam> = emptyList(),
    val postData: HarPostData? = null,
    val headersSize: Int = -1,
    val bodySize: Long
)

@Serializable
data class HarResponse(
    val status: Int,
    val statusText: String = "",
    val httpVersion: String = "HTTP/1.1",
    val headers: List<HarHeader>,
    val content: HarContent,
    val redirectURL: String = "",
    val headersSize: Int = -1,
    val bodySize: Long
)

@Serializable
data class HarHeader(val name: String, val value: String)

@Serializable
data class HarQueryParam(val name: String, val value: String)

@Serializable
data class HarPostData(
    val mimeType: String = "application/octet-stream",
    val text: String
)

@Serializable
data class HarContent(
    val size: Long,
    val mimeType: String,
    val text: String? = null,
    val encoding: String? = null
)

@Serializable
data class HarTimings(
    val send: Long = 0,
    val wait: Long = 0,
    val receive: Long = 0
)

@OptIn(ExperimentalTime::class)
internal fun TransactionFull.toHarEntry(): HarEntry {
    val started = Instant.fromEpochMilliseconds(sendTime).toString()

    val request = HarRequest(
        method = method,
        url = fullUrl,
        headers = requestHeaders.map { HarHeader(it.key, it.value) },
        bodySize = requestBody?.size?.toLong() ?: 0L,
        postData = requestBody?.let {
            HarPostData(
                mimeType = requestHeaders["Content-Type"] ?: "application/octet-stream",
                text = it.decodeToString() // if binary, Base64 it
            )
        }
    )

    val response = HarResponse(
        status = responseStatus ?: 0,
        headers = responseHeaders.map { HarHeader(it.key, it.value) },
        content = HarContent(
            size = responseBody?.size?.toLong() ?: 0L,
            mimeType = responseHeaders["Content-Type"] ?: "application/octet-stream",
            text = if(responseDefaultType == BodyType.IMAGE){
                responseBody?.encodeBase64()
            } else{
                responseBody?.decodeToString()
            }
        ),
        bodySize = responseBody?.size?.toLong() ?: 0L
    )

    val timings = HarTimings(
        wait = totalTime
    )

    return HarEntry(
        startedDateTime = started,
        time = totalTime,
        request = request,
        response = response,
        timings = timings
    )
}

internal fun List<TransactionFull>.toHarFile(): HarFile {
    return HarFile(
        log = HarLog(
            entries = this.map { it.toHarEntry() }
        )
    )
}
