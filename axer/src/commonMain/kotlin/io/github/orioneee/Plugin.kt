package io.github.orioneee

import io.github.orioneee.config.AxerConfig
import io.github.orioneee.domain.requests.Transaction
import io.github.orioneee.extentions.isValidImage
import io.github.orioneee.logger.getPlatformStackTrace
import io.github.orioneee.processors.RequestProcessor
import io.ktor.client.plugins.api.ClientPlugin
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
internal val AxerPlugin: ClientPlugin<AxerConfig> = createClientPlugin("Axer", ::AxerConfig) {
    on(Send) {
        Axer.initIfCan()
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
        if (!pluginConfig.requestFilter.invoke(request)) {
            return@on proceed(it)
        }
        val importantInRequest = pluginConfig.requestImportantSelector.invoke(request)
        val reduced = pluginConfig.requestReducer.invoke(request)
        state = state.copy(
            importantInRequest = importantInRequest,
            requestHeaders = reduced.headers,
            requestBody = reduced.body,
            method = reduced.method,
            host = reduced.host,
            path = reduced.path,
            sendTime = reduced.sendTime
        )
        val id = processor.onSend(state)
        state = state.copy(id = id)

        val response = try {
            proceed(it)
        } catch (e: Exception) {
            val stackTrace = e.getPlatformStackTrace()
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
            responseBody = if (isImage) null else try {
                response.response.bodyAsText()
            } catch (e: Exception) {
                "Error reading response body: ${e.message}"
            },
            responseTime = responseTime,
            responseHeaders = responseHeaders,
            responseStatus = responseStatus,
            imageBytes = if (isImage) responseBody else null,
            isImage = isImage
        )
        val resp = finishedState.asResponse()
        val importantInResponse = pluginConfig.responseImportantSelector.invoke(resp)
        val responseFiltred = pluginConfig.responseFilter.invoke(resp)
        if (responseFiltred == true) {
            val reducedResponse = pluginConfig.responseReducer.invoke(resp)
            processor.onFinished(
                finishedState.copy(
                    importantInResponse = importantInResponse,
                    responseHeaders = reducedResponse.headers,
                    responseBody = reducedResponse.body,
                    responseStatus = reducedResponse.status,
                    imageBytes = reducedResponse.image,
                    isImage = reducedResponse.image != null && reducedResponse.image.isNotEmpty()
                )
            )
        } else {
            processor.deleteRequestIfNotFiltred(finishedState.id)
        }
        response
    }
}