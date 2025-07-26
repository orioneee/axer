package io.github.orioneee

import io.github.orioneee.domain.requests.Request
import io.github.orioneee.processors.RequestProcessor
import io.github.orioneee.domain.requests.data.Transaction
import io.github.orioneee.domain.requests.data.TransactionFull
import io.github.orioneee.domain.requests.formatters.BodyType
import io.github.orioneee.extentions.isValidImage
import io.github.orioneee.extentions.toBodyType
import io.github.orioneee.logger.getSavableError
import io.ktor.http.contentType
import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import kotlin.time.Duration.Companion.hours

class AxerOkhttpInterceptor private constructor(
    private val requestImportantSelector: (Request) -> List<String>,
    private val responseImportantSelector: (io.github.orioneee.domain.requests.Response) -> List<String>,
    private val requestFilter: (Request) -> Boolean,
    private val responseFilter: (io.github.orioneee.domain.requests.Response) -> Boolean,
    private val requestReducer: (Request) -> Request = { request -> request },
    private val responseReducer: (io.github.orioneee.domain.requests.Response) -> io.github.orioneee.domain.requests.Response,
    private val requestMaxAgeInSeconds: Long,
    private val retentionSizeInBytes: Long,
    private val maxBodySize: Long = 250_000 // ~244 KB
) : Interceptor {
    init {
        Axer.initIfCan()
    }

    constructor(): this(
        requestImportantSelector = { emptyList() },
        responseImportantSelector = { emptyList() },
        requestFilter = { true },
        responseFilter = { true },
        requestReducer = { it },
        responseReducer = { it },
        requestMaxAgeInSeconds = 12.hours.inWholeSeconds, // 12 hour
        retentionSizeInBytes = 1024 * 1024 * 100, // 100 MB
        maxBodySize = 250_000 // ~244 KB
    )

    class Builder() {
        private var requestImportantSelector: (Request) -> List<String> = { emptyList() }
        private var responseImportantSelector: (io.github.orioneee.domain.requests.Response) -> List<String> =
            { emptyList() }
        private var requestFilter: (Request) -> Boolean = { true }
        private var requestReducer: (Request) -> Request = { it }
        private var responseFilter: (io.github.orioneee.domain.requests.Response) -> Boolean =
            { true }
        private var responseReducer: (io.github.orioneee.domain.requests.Response) -> io.github.orioneee.domain.requests.Response =
            { it }
        private var requestMaxAgeInSeconds: Long = 60 * 60 * 12 // 12 hour
        private var retentionSizeInBytes: Long = 1024 * 1024 * 100 // 100 MB
        private var maxBodySize: Long = 250_000 // ~244 KB

        fun setMaxBodySize(sizeInBytes: Long) = apply {
            this.maxBodySize = sizeInBytes
        }

        fun setRequestImportantSelector(selector: (Request) -> List<String>) = apply {
            this.requestImportantSelector = selector
        }


        fun setRequestReducer(reducer: (Request) -> Request) = apply {
            this.requestReducer = reducer
        }

        fun setResponseFilter(filter: (io.github.orioneee.domain.requests.Response) -> Boolean) =
            apply {
                this.responseFilter = filter
            }

        fun setResponseImportantSelector(selector: (io.github.orioneee.domain.requests.Response) -> List<String>) =
            apply {
                this.responseImportantSelector = selector
            }

        fun setRequestFilter(filter: (Request) -> Boolean) = apply {
            this.requestFilter = filter
        }


        fun setResponseReducer(reducer: (io.github.orioneee.domain.requests.Response) -> io.github.orioneee.domain.requests.Response) =
            apply {
                this.responseReducer = reducer
            }

        fun setRetentionTime(seconds: Long) = apply {
            this.requestMaxAgeInSeconds = seconds
        }

        fun setRetentionSize(sizeInBytes: Long) = apply {
            this.retentionSizeInBytes = sizeInBytes
        }

        fun build() = AxerOkhttpInterceptor(
            requestImportantSelector = requestImportantSelector,
            responseImportantSelector = responseImportantSelector,
            requestFilter = requestFilter,
            responseFilter = responseFilter,
            requestReducer = requestReducer,
            responseReducer = responseReducer,
            requestMaxAgeInSeconds = requestMaxAgeInSeconds,
            retentionSizeInBytes = retentionSizeInBytes
        )
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        return runBlocking {
            val processor = RequestProcessor(requestMaxAgeInSeconds, retentionSizeInBytes)
            val request = chain.request()
            val sendTime = System.currentTimeMillis()
            val method = request.method
            val host = request.url.host
            val path = request.url.encodedPath
            val query = request.url.query
            val requestHeaders =
                request.headers.toMultimap().mapValues { it.value.joinToString(", ") }
            val requestBody = request.body?.let { body ->
                val buffer = okio.Buffer()
                body.writeTo(buffer)
                buffer.readByteArray()
            }?.let {
                val bodySize = it.size
                if (bodySize > maxBodySize) {
                    "Body is to large, current max size is ${maxBodySize} bytes but got $bodySize bytes"
                        .toByteArray()
                } else {
                    it
                }
            }

            var transaction = TransactionFull(
                sendTime = sendTime,
                method = method,
                host = host,
                path = path + if (query != null) "?$query" else "",
                requestHeaders = requestHeaders,
                requestBody = requestBody
            )

            val requestModel = transaction.asRequest()
            if (!requestFilter(requestModel)) {
                return@runBlocking chain.proceed(request)
            }
            val importantInRequest = requestImportantSelector(requestModel)
            val reducedRequest = requestReducer(requestModel)
            transaction = transaction.copy(
                importantInRequest = importantInRequest,
                requestHeaders = reducedRequest.headers,
                requestBody = reducedRequest.body,
                method = reducedRequest.method,
                host = reducedRequest.host,
                path = reducedRequest.path,
                sendTime = reducedRequest.sendTime
            )
            val id = processor.onSend(transaction)
            transaction = transaction.copy(id = id)

            try {
                val response = chain.proceed(request)
                val responseTime = System.currentTimeMillis()

                val responseHeaders =
                    response.headers.toMultimap().mapValues { it.value.joinToString(", ") }
                val (responseBodyBytes, contentType) = response.body.bytes().let {
                    val bodySize = it.size
                    if (bodySize > maxBodySize) {
                        "Body is to large, current max size is ${maxBodySize} bytes but got $bodySize bytes"
                            .toByteArray() to BodyType.RAW_TEXT
                    } else {
                        it to if (it.isValidImage()) {
                            BodyType.IMAGE
                        } else {
                            response.body.contentType()?.toBodyType() ?: BodyType.RAW_TEXT
                        }
                    }
                }

                val finishedTransaction = transaction.updateToFinished(
                    responseBody = responseBodyBytes,
                    responseTime = responseTime,
                    responseHeaders = responseHeaders,
                    responseStatus = response.code,
                    bodyType = contentType
                )

                val responseModel = finishedTransaction.asResponse()
                val responseFiltred = responseFilter(responseModel)
                if (responseFiltred) {
                    val importantInResponse = responseImportantSelector(responseModel)
                    val reducedResponse = responseReducer(responseModel)
                    val finishedState = finishedTransaction.copy(
                        importantInResponse = importantInResponse,
                        responseHeaders = reducedResponse.headers,
                        responseBody = reducedResponse.body,
                        responseStatus = reducedResponse.status,
                        responseTime = reducedResponse.time,
                        responseDefaultType = reducedResponse.bodyType
                    )
                    processor.onFinished(finishedState)
                } else {
                    processor.deleteRequestIfNotFiltred(finishedTransaction.id)
                }


                response.newBuilder()
                    .body(responseBodyBytes.toResponseBody(response.body?.contentType()))
                    .build()

            } catch (e: Exception) {
                val errorState = transaction.updateToError(e.getSavableError())
                processor.onFailed(errorState)
                throw e
            }
        }
    }
}
