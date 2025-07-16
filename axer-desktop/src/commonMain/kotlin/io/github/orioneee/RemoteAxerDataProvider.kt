package io.github.orioneee

import io.github.orioneee.domain.database.DatabaseWrapped
import io.github.orioneee.domain.exceptions.AxerException
import io.github.orioneee.domain.logs.LogLine
import io.github.orioneee.domain.requests.Transaction
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.net.URI

class RemoteAxerDataProvider @OptIn(DelicateCoroutinesApi::class) constructor(
    private val serverUrl: String = "http://192.168.0.138:9000",
    private val scope: CoroutineScope = GlobalScope
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

    @OptIn(DelicateCoroutinesApi::class)
    private fun <T> webSocketFlow(
        path: String,
        deserializer: (String) -> T
    ): Flow<T> = callbackFlow {
        println("Connecting to WebSocket at $serverUrl$path")
        val uri = URI(serverUrl)
        val job = launch {
            while (isActive && !isClosedForSend) {
                try {
                    client.webSocket(
                        method = HttpMethod.Get,
                        host = uri.host,
                        port = uri.port,
                        path = path
                    ) {
                        val incomingFrames: ReceiveChannel<Frame> = incoming
                        try {
                            for (frame in incomingFrames) {
                                if (frame is Frame.Text) {
                                    val data = frame.readText()
                                    val obj = deserializer(data)
                                    println("Received data: ${data.toByteArray().size} bytes")
                                    trySend(obj).isSuccess
                                }
                            }
                        } catch (e: Exception) {
                            println("Error in frame processing: ${e.message}")
                        }
                    }
                } catch (e: Exception) {
                    println("WebSocket connection failed: ${e.message}")
                }

                delay(300)
            }
        }

        awaitClose {
            println("Closing WebSocket connection at $serverUrl$path")
            job.cancel()
        }
    }.shareIn(scope, replay = 1, started = kotlinx.coroutines.flow.SharingStarted.Lazily)


    override fun getAllRequests(): Flow<List<Transaction>> =
        webSocketFlow("/ws/requests") {
            println("Fetching all requests")
            json.decodeFromString(it)
        }

    override fun getRequestById(id: Long): Flow<Transaction?>{
        println("Request flow for ID: $id")
        return webSocketFlow("/ws/requests/$id") {
            json.decodeFromString(it)
        }
    }

    override suspend fun markAsViewed(id: Long) {
        println("Marking request as viewed: $id")
        val response = client.post("$serverUrl/requests/view/$id")
        if (!response.status.isSuccess()) throw Exception("Failed to mark viewed")
    }

    override suspend fun deleteAllRequests() {
        val response = client.delete("${serverUrl}/requests")
        println("Delete all requests response: ${response.status}")
        if (!response.status.isSuccess()) throw Exception("Failed to delete all requests")
    }

    override fun getAllExceptions(): Flow<List<AxerException>> =
        webSocketFlow("/ws/exceptions") {
            println("Fetching all exceptions")
            json.decodeFromString(it)
        }

    override fun getExceptionById(id: Long): Flow<AxerException?> =
        webSocketFlow("/ws/exceptions/$id") {
            println("Fetching exception by ID: $id")
            json.decodeFromString(it)
        }

    override suspend fun deleteAllExceptions() {
        val response = client.delete("$serverUrl/exceptions")
        if (!response.status.isSuccess()) throw Exception("Failed to delete all exceptions")
    }

    override fun getAllLogs(): Flow<List<LogLine>> =
        webSocketFlow("/ws/logs") {
            println("Fetching all logs")
            json.decodeFromString(it)
        }

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