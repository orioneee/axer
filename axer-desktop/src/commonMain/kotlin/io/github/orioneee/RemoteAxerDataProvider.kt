package io.github.orioneee

import io.github.orioneee.domain.database.DatabaseData
import io.github.orioneee.domain.database.DatabaseWrapped
import io.github.orioneee.domain.database.EditableRowItem
import io.github.orioneee.domain.database.QueryResponse
import io.github.orioneee.domain.database.RowItem
import io.github.orioneee.domain.exceptions.AxerException
import io.github.orioneee.domain.logs.LogLine
import io.github.orioneee.domain.other.EnabledFeathers
import io.github.orioneee.domain.requests.Transaction
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.net.URI
import kotlin.math.abs
import kotlin.system.exitProcess

class RemoteAxerDataProvider(
    private val serverUrl: String,
) : AxerDataProvider {
    private val client = HttpClient {
        install(WebSockets.Plugin) {
            contentConverter = KotlinxWebsocketSerializationConverter(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                }
            )
        }
        install(ContentNegotiation) {
            json(
                Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
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
        val uri = URI(serverUrl)
        val job = launch {
            while (isActive && !isClosedForSend) {
                try {
                    client.webSocket(
                        method = HttpMethod.Companion.Get,
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
                                    trySend(obj).isSuccess
                                }
                            }
                        } catch (e: Exception) {
                        }
                    }
                } catch (e: Exception) {
                }

                delay(1_000)
            }
        }

        awaitClose {
            job.cancel()
        }
    }.distinctUntilChanged()


    override fun getAllRequests(): Flow<List<Transaction>> =
        webSocketFlow("/ws/requests") {
            val decoded = json.decodeFromString<List<String>>(it)
            val transactions = decoded.map { str ->
                json.decodeFromString<Transaction>(str)
            }

            transactions
        }

    override fun getRequestById(id: Long): Flow<Transaction?> {
        return webSocketFlow("/ws/requests/$id") {
            json.decodeFromString(it)
        }
    }

    override suspend fun markAsViewed(id: Long) {
        val response = client.post("$serverUrl/requests/view/$id")
        if (!response.status.isSuccess()) throw Exception("Failed to mark viewed")
    }

    override suspend fun deleteAllRequests() {
        val response = client.delete("${serverUrl}/requests")
        if (!response.status.isSuccess()) throw Exception("Failed to delete all requests")
    }

    override fun getAllExceptions(): Flow<List<AxerException>> =
        webSocketFlow("/ws/exceptions") {
            json.decodeFromString(it)
        }

    override fun getExceptionById(id: Long): Flow<AxerException?> =
        webSocketFlow("/ws/exceptions/$id") {
            json.decodeFromString(it)
        }

    override suspend fun deleteAllExceptions() {
        val response = client.delete("$serverUrl/exceptions")
        if (!response.status.isSuccess()) throw Exception("Failed to delete all exceptions")
    }

    override fun getAllLogs(): Flow<List<LogLine>> =
        webSocketFlow("/ws/logs") {
            json.decodeFromString(it)
        }

    override suspend fun deleteAllLogs() {
        val response = client.delete("$serverUrl/logs")
        if (!response.status.isSuccess()) throw Exception("Failed to delete all logs")
    }

    override fun getDatabases(): Flow<List<DatabaseWrapped>> =
        webSocketFlow("/ws/database") { json.decodeFromString(it) }

    override fun getDatabaseContent(
        file: String,
        tableName: String,
        page: Int,
        pageSize: Int
    ): Flow<DatabaseData> {
        return webSocketFlow("/ws/database/$file/$tableName/$page") { data ->
            json.decodeFromString(data)
        }
    }

    override suspend fun clearTable(file: String, tableName: String) {
        val response = client.delete("$serverUrl/database/$file/$tableName")
        if (!response.status.isSuccess()) {
            throw Exception("Failed to clear table: ${response.status}")
        }
    }

    override fun getAllQueries(): Flow<String> {
        return webSocketFlow("/ws/db_queries") { data ->
            data
        }
    }

    override suspend fun updateCell(
        file: String,
        tableName: String,
        editableItem: EditableRowItem
    ) {
        val response = client.post("$serverUrl/database/cell/$file/$tableName") {
            contentType(ContentType.Application.Json)
            setBody(editableItem)
        }
    }

    override suspend fun deleteRow(
        file: String,
        tableName: String,
        row: RowItem
    ) {
        val response = client.delete("$serverUrl/database/row/$file/$tableName") {
            contentType(ContentType.Application.Json)
            setBody(row)
        }
        if (!response.status.isSuccess()) {
            throw Exception("Failed to delete row: ${response.status}")
        }
    }

    override suspend fun executeRawQuery(file: String, query: String) {
        val response = client.post("$serverUrl/ws/db_queries/execute/$file") {
            contentType(ContentType.Text.Plain)
            setBody(query)
        }

        if (!response.status.isSuccess()) {
            throw Exception("Failed to execute query: ${response.status}")
        }
    }


    override fun executeRawQueryAndGetUpdates(
        file: String,
        query: String
    ): Flow<QueryResponse> = callbackFlow {
        val uri = URI(serverUrl)
        val job = launch {
            try {
                client.webSocket(
                    method = HttpMethod.Companion.Get,
                    host = uri.host,
                    port = uri.port,
                    path = "/ws/db_queries/execute_and_get_updates/$file"
                ) {
                    send(Frame.Text(query))
                    for (frame in incoming) {
                        if (frame is Frame.Text) {
                            val text = frame.readText()
                            try {
                                val response = json.decodeFromString<QueryResponse>(text)
                                trySend(response).isSuccess
                            } catch (e: Exception) {
                            }
                        }
                    }
                }
            } catch (e: Exception) {
            }
        }

        awaitClose {
            job.cancel()
        }
    }

    override fun isConnected(): Flow<Boolean> {
        val maxDelta = 3_000L

        val serverTimeFlow = webSocketFlow("/ws/isAlive") { msg ->
            val m = msg.substringAfter("ping - ").replace("\"", "").toLongOrNull() ?: 0L
            m
        }
        val clientTimeFlow = flow {
            while (true) {
                emit(System.currentTimeMillis())
                delay(1000)
            }
        }

        return combine(
            serverTimeFlow,
            clientTimeFlow
        ) { serverTime, clientTime ->
            abs(serverTime - clientTime) < maxDelta
        }.distinctUntilChanged()
    }


    override fun getEnabledFeatures(): Flow<EnabledFeathers> {
        return webSocketFlow("/ws/feathers") { data ->
            json.decodeFromString(data)
        }
    }


    suspend fun close() {
        client.close()
    }
}