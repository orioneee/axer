package io.github.orioneee

import io.github.orioneee.internal.domain.requests.data.TransactionFull
import io.github.orioneee.internal.domain.requests.formatters.BodyType
import io.github.orioneee.internal.extentions.isValidImage
import io.github.orioneee.internal.extentions.toBodyType
import io.github.orioneee.internal.logger.getSavableError
import io.github.orioneee.internal.processors.RequestProcessor
import io.github.orioneee.internal.processors.SessionManager
import io.ktor.client.plugins.api.ClientPlugin
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.content.OutgoingContent
import io.ktor.http.contentType
import io.ktor.utils.io.core.toByteArray
import io.ktor.utils.io.readRemaining
import kotlinx.io.readByteArray
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal val AxerPlugin: ClientPlugin<AxerKtorPluginConfig> =
    createClientPlugin("Axer", ::AxerKtorPluginConfig) {
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
                    t.bytes()
                }

                is OutgoingContent.ReadChannelContent -> {
                    val channel = (it.body as OutgoingContent.ReadChannelContent).readFrom()
                    channel.readRemaining().readByteArray()
                }

                is OutgoingContent.WriteChannelContent -> {
                    (it.body as OutgoingContent.WriteChannelContent).toString().toByteArray()
                }

                is OutgoingContent.NoContent -> {
                    null
                }

                is OutgoingContent.ProtocolUpgrade -> {
                    null
                }

                else -> {
                    it.body.toString().toByteArray()
                }
            }?.let {
                val bodySize = it.size
                if (bodySize > pluginConfig.maxBodySize) {
                    "Body is to large, current max size is ${pluginConfig.maxBodySize} bytes but got $bodySize bytes"
                        .toByteArray()
                } else {
                    it
                }
            }
            val processor = RequestProcessor(
                pluginConfig.retentionPeriodInSeconds,
                pluginConfig.retentionSizeInBytes,
            )
            var state = TransactionFull(
                sendTime = sendTime,
                method = method,
                host = host,
                path = path,
                requestHeaders = requestHeaders,
                requestBody = requestBody,
                sessionIdentifier = SessionManager.sessionId
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
                val error = e.getSavableError()
                processor.onFailed(state.updateToError(error, Clock.System.now().toEpochMilliseconds()))
                throw e
            }
            val responseTime = Clock.System.now().toEpochMilliseconds()
            val responseHeaders = response.response.headers.entries()
                .associate { entry -> entry.key to entry.value.joinToString(", ") }
            val (responseBody, contentType) = response.response.bodyAsBytes().let {
                val bodySize = it.size
                if (bodySize > pluginConfig.maxBodySize) {
                    "Body is to large, current max size is ${pluginConfig.maxBodySize} bytes but got $bodySize bytes"
                        .toByteArray() to BodyType.RAW_TEXT
                } else {
                    it to if (it.isValidImage()) BodyType.IMAGE
                    else response.response.contentType().toBodyType()
                }
            }

            val responseStatus = response.response.status.value
            val finishedState = state.updateToFinished(
                responseBody = responseBody,
                responseTime = responseTime,
                responseHeaders = responseHeaders,
                responseStatus = responseStatus,
                bodyType = contentType
            )
            val resp = finishedState.asResponse()
            val importantInResponse = pluginConfig.responseImportantSelector.invoke(resp)
            val responseFiltred = pluginConfig.responseFilter.invoke(resp)
            if (responseFiltred) {
                val reducedResponse = pluginConfig.responseReducer.invoke(resp)
                processor.onFinished(
                    finishedState.copy(
                        importantInResponse = importantInResponse,
                        responseHeaders = reducedResponse.headers,
                        responseBody = reducedResponse.body,
                        responseStatus = reducedResponse.status,
                        responseDefaultType = reducedResponse.bodyType
                    )
                )
            } else {
                processor.deleteRequestIfNotFiltred(finishedState.id)
            }
            response
        }
    }