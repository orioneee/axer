package io.github.orioneee

import io.github.orioneee.domain.requests.Request
import io.github.orioneee.processors.RequestProcessor
import io.github.orioneee.domain.requests.Transaction
import io.github.orioneee.extentions.isValidImage
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

class AxerOkhttpInterceptor private constructor(
    private val requestImportantSelector: (Request) -> List<String>,
    private val responseImportantSelector: (io.github.orioneee.domain.requests.Response) -> List<String>,
    private val requestFilter: (Request) -> Boolean,
    private val responseFilter: (io.github.orioneee.domain.requests.Response) -> Boolean,
    private val requestReducer: (Request) -> Request = { request -> request },
    private val responseReducer: (io.github.orioneee.domain.requests.Response) -> io.github.orioneee.domain.requests.Response,
    private val requestMaxAgeInSeconds: Long
) : Interceptor {
    init {
        Axer.initIfCan()
    }

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
        private var requestMaxAgeInSeconds: Long = 60 * 60 * 1

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

        fun build() = AxerOkhttpInterceptor(
            requestImportantSelector = requestImportantSelector,
            responseImportantSelector = responseImportantSelector,
            requestFilter = requestFilter,
            responseFilter = responseFilter,
            requestReducer = requestReducer,
            responseReducer = responseReducer,
            requestMaxAgeInSeconds = requestMaxAgeInSeconds
        )
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        return runBlocking {
            val processor = RequestProcessor(requestMaxAgeInSeconds)
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
                buffer.readUtf8()
            }

            var transaction = Transaction(
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
                val responseBodyBytes = response.body.bytes()
                val contentType = response.body.contentType()
                val isImageContentType = contentType?.let {
                    it.type == "image" || it.subtype in listOf(
                        "jpeg", "png", "gif", "webp", "svg+xml"
                    )
                } == true
                val isImage = isImageContentType || responseBodyBytes.isValidImage()

                val finishedTransaction = transaction.updateToFinished(
                    responseBody = if (isImage) null else responseBodyBytes.decodeToString(),
                    responseTime = responseTime,
                    responseHeaders = responseHeaders,
                    responseStatus = response.code,
                    imageBytes = if (isImage) responseBodyBytes else null,
                    isImage = isImage
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
                        imageBytes = reducedResponse.image,
                        isImage = reducedResponse.image != null && reducedResponse.image.isNotEmpty(),
                    )
                    processor.onFinished(finishedState)
                } else {
                    processor.deleteRequestIfNotFiltred(finishedTransaction.id)
                }


                response.newBuilder()
                    .body(responseBodyBytes.toResponseBody(response.body?.contentType()))
                    .build()

            } catch (e: Exception) {
                val errorState = transaction.updateToError(e.stackTraceToString())
                processor.onFailed(errorState)
                throw e
            }
        }
    }
}
