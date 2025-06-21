package com.oriooneee.ktorin

import com.oriooneee.ktorin.config.RequestImportantSelector
import com.oriooneee.ktorin.config.ResponseImportantSelector
import com.oriooneee.ktorin.requestProcessor.RequestProcessor
import com.oriooneee.ktorin.room.entities.Transaction
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

class KtorinOkhttpInterceptor private constructor(
    private val requestImportantSelector: RequestImportantSelector,
    private val responseImportantSelector: ResponseImportantSelector,
) : Interceptor {

    class Builder(){
        private var requestImportantSelector: RequestImportantSelector = object : RequestImportantSelector {
            override suspend fun selectImportant(request: com.oriooneee.ktorin.room.entities.Request): List<String> {
                return emptyList()
            }
        }

        private var responseImportantSelector: ResponseImportantSelector = object : ResponseImportantSelector {
            override suspend fun selectImportant(response: com.oriooneee.ktorin.room.entities.Response): List<String> {
                return emptyList()
            }
        }

        fun setRequestImportantSelector(selector: RequestImportantSelector) = apply {
            this.requestImportantSelector = selector
        }

        fun setResponseImportantSelector(selector: ResponseImportantSelector) = apply {
            this.responseImportantSelector = selector
        }

        fun build() = KtorinOkhttpInterceptor(requestImportantSelector, responseImportantSelector)
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
            val importantInRequest = requestImportantSelector.selectImportant(requestModel)
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
                val importantInResponse = responseImportantSelector.selectImportant(responseModel)
                processor.onFinished(finishedTransaction.copy(importantInResponse = importantInResponse))

                // Build new response with consumed body
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
