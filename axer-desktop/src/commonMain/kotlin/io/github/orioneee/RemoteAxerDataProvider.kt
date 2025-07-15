package io.github.orioneee

import io.github.orioneee.domain.database.DatabaseWrapped
import io.github.orioneee.domain.exceptions.AxerException
import io.github.orioneee.domain.logs.LogLine
import io.github.orioneee.domain.requests.Transaction
import io.github.orioneee.provider.AxerDataProvider
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.http.HttpMethod
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.net.URI

class RemoteAxerDataProvider(
    private val serverUrl: String = "http://192.168.0.165:9000"
) : AxerDataProvider {

    private val client = HttpClient {
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                }
            )
        }
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private fun <T> webSocketFlow(
        path: String,
        deserializer: (String) -> T
    ): Flow<T> = callbackFlow {
        val uri = URI(serverUrl)
        client.webSocket(
            method = HttpMethod.Get,
            host = uri.host,
            port = uri.port,
            path = path
        ) {
            val incomingFrames: ReceiveChannel<Frame> = incoming
            launch {
                try {
                    for (frame in incomingFrames) {
                        if (frame is Frame.Text) {
                            val data = frame.readText()
                            val obj = deserializer(data)
                            trySend(obj)
                        }
                    }
                } catch (e: Exception) {
                    close(e)
                }
            }
            awaitClose { this.cancel() }
        }
    }


    override fun getAllRequests(): Flow<List<Transaction>> =
        webSocketFlow("/ws/requests") { json.decodeFromString(it) }

    override fun getRequestById(id: Long): Flow<Transaction?> =
        webSocketFlow("/ws/requests/$id") { json.decodeFromString(it) }

    override suspend fun markAsViewed(id: Long) {
        val response = client.post("$serverUrl/requests/viewed/$id")
        if (!response.status.isSuccess()) throw Exception("Failed to mark viewed")
    }

    override suspend fun deleteAllRequests() {
        val response = client.delete("$serverUrl/requests")
        if (!response.status.isSuccess()) throw Exception("Failed to delete all requests")
    }

    override fun getAllExceptions(): Flow<List<AxerException>> =
        webSocketFlow("/ws/exceptions") { json.decodeFromString(it) }

    override fun getExceptionById(id: Long): Flow<AxerException?> =
        webSocketFlow("/ws/exceptions/$id") { json.decodeFromString(it) }

    override suspend fun deleteAllExceptions() {
        val response = client.delete("$serverUrl/exceptions")
        if (!response.status.isSuccess()) throw Exception("Failed to delete all exceptions")
    }

    override fun getAllLogs(): Flow<List<LogLine>> =
        webSocketFlow("/ws/logs") { json.decodeFromString(it) }

    override suspend fun deleteAllLogs() {
        val response = client.delete("$serverUrl/logs")
        if (!response.status.isSuccess()) throw Exception("Failed to delete all logs")
    }

    override fun getDatabases(): Flow<List<DatabaseWrapped>> =
        webSocketFlow("/ws/database") { json.decodeFromString(it) }

    suspend fun close() {
        client.close()
    }
}