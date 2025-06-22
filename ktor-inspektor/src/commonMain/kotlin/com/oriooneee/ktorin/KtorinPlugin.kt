package com.oriooneee.ktorin

import com.oriooneee.ktorin.config.KtorinConfig
import com.oriooneee.ktorin.requestProcessor.RequestProcessor
import com.oriooneee.ktorin.domain.Transaction
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.statement.bodyAsBytes
import io.ktor.client.statement.bodyAsText
import io.ktor.http.content.OutgoingContent
import io.ktor.utils.io.readRemaining
import kotlinx.io.readByteArray
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
val KtorinPlugin = createClientPlugin("Ktorin", ::KtorinConfig) {
    on(Send) {
        val sendTime = Clock.System.now().toEpochMilliseconds()
        val method = it.method.value
        val host = it.url.host
        val path = it.url.toString().substringAfter(host)
        val requestHeaders: Map<String, String> = it.headers.entries()
            .associate { entry -> entry.key to entry.value.joinToString(", ") }

        val requestBody = when (it.body) {
            is OutgoingContent.ByteArrayContent -> {
                val t = it.body as OutgoingContent.ByteArrayContent
                t.bytes().decodeToString()
            }

            is OutgoingContent.ReadChannelContent -> {
                val channel = (it.body as OutgoingContent.ReadChannelContent).readFrom()
                channel.readRemaining().readByteArray().decodeToString()
            }

            is OutgoingContent.WriteChannelContent -> {
                val channel = (it.body as OutgoingContent.WriteChannelContent).toString()
                channel
            }

            is OutgoingContent.NoContent -> {
                null
            }

            is OutgoingContent.ProtocolUpgrade -> {
                null
            }

            else -> {
                it.body.toString()
            }
        }
        val processor = RequestProcessor()
        var state = Transaction(
            sendTime = sendTime,
            method = method,
            host = host,
            path = path,
            requestHeaders = requestHeaders,
            requestBody = requestBody,
        )
        val request = state.asRequest()
        val importantInRequest =
            pluginConfig.requestImportantSelector.selectImportant(request)
        state = state.copy(importantInRequest = importantInRequest)
        val id = processor.onSend(state)
        state = state.copy(id = id)

        val response = try {
            proceed(it)
        } catch (e: Exception) {
            val stackTrace = e.stackTraceToString()
            processor.onFailed(state.updateToError(stackTrace))
            throw e
        }
        val responseTime = Clock.System.now().toEpochMilliseconds()
        val responseHeaders = response.response.headers.entries()
            .associate { entry -> entry.key to entry.value.joinToString(", ") }
        val responseBody = response.response.bodyAsBytes()
        val isImage = responseBody.isValidImage()


        val responseStatus = response.response.status.value
        val finishedState = state.updateToFinished(
            responseBody = if (isImage) null else response.response.bodyAsText(),
            responseTime = responseTime,
            responseHeaders = responseHeaders,
            responseStatus = responseStatus,
            imageBytes = if (isImage) responseBody else null,
            isImage = isImage
        )
        val resp = finishedState.asResponse()
        val importantInResponse =
            pluginConfig.responseImportantSelector.selectImportant(resp)
        processor.onFinished(finishedState.copy(importantInResponse = importantInResponse))
        response
    }
}
