package com.oriooneee.axer

import com.oriooneee.axer.domain.Request
import com.oriooneee.axer.requestProcessor.RequestProcessor
import com.oriooneee.axer.domain.Transaction
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

class AxerOkhttpInterceptor private constructor(
    private val requestImportantSelector: (Request) -> List<String>,
    private val responseImportantSelector: (com.oriooneee.axer.domain.Response) -> List<String>,
    private val requestFilter: (Request) -> Boolean,
) : Interceptor {

    class Builder(){
        private var requestImportantSelector: (Request) -> List<String> = { request ->
            emptyList()
        }

        private var responseImportantSelector: (com.oriooneee.axer.domain.Response) -> List<String> = { response ->
            emptyList()
        }

        private var requestFilter: (Request) -> Boolean = { request ->
            true
        }

        fun setRequestImportantSelector(selector: (Request) -> List<String>) = apply {
            this.requestImportantSelector = selector
        }

        fun setResponseImportantSelector(selector: (com.oriooneee.axer.domain.Response) -> List<String>) = apply {
            this.responseImportantSelector = selector
        }

        fun setRequestFilter(filter: (Request) -> Boolean) = apply {
            this.requestFilter = filter
        }

        fun build() = AxerOkhttpInterceptor(
            requestImportantSelector = requestImportantSelector,
            responseImportantSelector = responseImportantSelector,
            requestFilter = requestFilter
        )
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        return runBlocking {
            val processor = RequestProcessor()
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
            transaction = transaction.copy(importantInRequest = importantInRequest)
            val id = processor.onSend(transaction)
            transaction = transaction.copy(id = id)

            try {
                val response = chain.proceed(request)
//                throw Exception("Test exception")
                val responseTime = System.currentTimeMillis()

                val responseHeaders =
                    response.headers.toMultimap().mapValues { it.value.joinToString(", ") }
                val responseBodyBytes = response.body?.bytes() ?: ByteArray(0)
                val isImage = responseBodyBytes.isValidImage()

                val finishedTransaction = transaction.updateToFinished(
                    responseBody = if (isImage) null else responseBodyBytes.decodeToString(),
                    responseTime = responseTime,
                    responseHeaders = responseHeaders,
                    responseStatus = response.code,
                    imageBytes = if (isImage) responseBodyBytes else null,
                    isImage = isImage
                )

                val responseModel = finishedTransaction.asResponse()
                val importantInResponse = responseImportantSelector(responseModel)
                processor.onFinished(finishedTransaction.copy(importantInResponse = importantInResponse))

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
