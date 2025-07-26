package io.github.orioneee

import io.github.orioneee.domain.database.DatabaseData
import io.github.orioneee.domain.database.DatabaseWrapped
import io.github.orioneee.domain.database.EditableRowItem
import io.github.orioneee.domain.database.QueryResponse
import io.github.orioneee.domain.database.RowItem
import io.github.orioneee.domain.exceptions.AxerException
import io.github.orioneee.domain.logs.LogLine
import io.github.orioneee.domain.other.DataState
import io.github.orioneee.domain.other.EnabledFeathers
import io.github.orioneee.domain.requests.data.TransactionFull
import io.github.orioneee.domain.requests.data.TransactionShort
import io.github.orioneee.remote.server.UpdatesData
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.net.URI
import kotlin.math.abs

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
    private inline fun <reified T> webSocketFlow(
        path: String,
        crossinline dataMapper: (T) -> T = { it },
    ): Flow<DataState<T>> = callbackFlow {
        val uri = URI(serverUrl)
        trySend(DataState.Loading())
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
                                    val sizeInBytes = data.toByteArray().size
                                    println("Received data from: $path, size: $sizeInBytes bytes")
                                    val obj = json.decodeFromString<T>(data)
                                    val mapped = dataMapper(obj)
                                    trySend(DataState.Success(mapped)).isSuccess
                                }
                            }
                        } catch (e: Exception) {
                            println("Error processing frame: ${e.message} from path: $path")
                        }
                    }
                } catch (e: Exception) {
                    println("WebSocket error: ${e.message}")
                }

                delay(1_000)
            }
        }

        awaitClose {
            job.cancel()
        }
    }.distinctUntilChanged()

    private inline fun <reified T : Any> webSocketUpdatesFlow(
        path: String,
        crossinline getId: (T) -> Long,
        crossinline sorter: (List<T>) -> List<T>
    ): Flow<DataState<List<T>>> {
        val currentState = mutableListOf<T>()

        return webSocketFlow<UpdatesData<T>>(path) // assuming raw data is String
            .map { state ->
                when (state) {
                    is DataState.Loading -> {
                        DataState.Loading()
                    }

                    is DataState.Success<*> -> {
                        val update = state.data as UpdatesData<T>
                        println("Received update: ${update.updatedOrCreated.size} updated, ${update.deleted.size} deleted, replaceWith size: ${update.replaceWith.size}")
                        if(update.replaceWith.isEmpty() && !update.replaceAll) {
                            // Remove deleted items
                            currentState.removeAll { oldItem ->
                                update.deleted.contains(getId(oldItem))
                            }

                            // Update or add new items
                            update.updatedOrCreated.forEach { newItem ->
                                val index = currentState.indexOfFirst { getId(it) == getId(newItem) }
                                if (index != -1) {
                                    currentState[index] = newItem
                                } else {
                                    currentState.add(newItem)
                                }
                            }
                        } else{
                            println("Replacing all items with new list of size: ${update.replaceWith.size}")
                            // Replace all items with the new list
                            currentState.clear()
                            currentState.addAll(update.replaceWith)
                        }
                        DataState.Success(sorter(currentState).toList())
                    }
                }
            }.distinctUntilChanged()
    }


    override fun getAllRequests(): Flow<DataState<List<TransactionShort>>> =
        webSocketUpdatesFlow(
            path = "/ws/requests",
            getId = { it.id },
            sorter = { it.sortedByDescending { it.sendTime } }
        )

    private suspend inline fun <reified T> safeRequest(
        crossinline requestBlock: suspend () -> HttpResponse
    ): Result<T> {
        return try {
            val response = requestBlock()
            if (!response.status.isSuccess()) {
                return Result.failure(Exception("Request failed with status: ${response.status}"))
            }
            val textBody = response.bodyAsText()
            Result.success(json.decodeFromString<T>(textBody))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun getDataForExportAsHar(): Result<List<TransactionFull>> =
        safeRequest {
            client.get("$serverUrl/requests/full")
        }

    override fun getRequestById(id: Long): Flow<DataState<TransactionFull?>> {
        return webSocketFlow("/ws/requests/$id")
    }

    override suspend fun markAsViewed(id: Long): Result<Unit> {
        return safeRequest<Unit> {
            client.post("$serverUrl/requests/view/$id")
        }
    }

    override suspend fun deleteAllRequests(): Result<Unit> {
        return safeRequest {
            client.delete("$serverUrl/requests")
        }
    }

    override fun getAllExceptions(): Flow<DataState<List<AxerException>>> =
        webSocketUpdatesFlow(
            path = "/ws/exceptions",
            getId = { it.id },
            sorter = { it.sortedByDescending { it.time } }
        )


    override fun getExceptionById(id: Long): Flow<DataState<AxerException?>> =
        webSocketFlow("/ws/exceptions/$id")

    override suspend fun deleteAllExceptions(): Result<Unit> {
        return safeRequest {
            client.delete("$serverUrl/exceptions")
        }
    }

    override fun getAllLogs(): Flow<DataState<List<LogLine>>> =
        webSocketUpdatesFlow(
            path = "/ws/logs",
            getId = { it.id },
            sorter = { it.sortedByDescending { it.time } }
        )


    override suspend fun deleteAllLogs(): Result<Unit> {
        return safeRequest {
            client.delete("$serverUrl/logs")
        }
    }

    override fun getDatabases(): Flow<DataState<List<DatabaseWrapped>>> =
        webSocketFlow("/ws/database")

    override fun getDatabaseContent(
        file: String,
        tableName: String,
        page: Int,
        pageSize: Int
    ): Flow<DataState<DatabaseData>> {
        return webSocketFlow("/ws/database/$file/$tableName/$page")
    }

    override suspend fun clearTable(file: String, tableName: String) {
        safeRequest<String> {
            client.delete("$serverUrl/database/$file/$tableName")
        }
    }

    override fun getAllQueries(): Flow<DataState<String>> {
        return webSocketFlow("/ws/db_queries")
    }

    override suspend fun updateCell(
        file: String,
        tableName: String,
        editableItem: EditableRowItem
    ): Result<Unit> {
        return safeRequest {
            client.post("$serverUrl/database/cell/$file/$tableName") {
                contentType(ContentType.Application.Json)
                setBody(editableItem)
            }
        }
    }

    override suspend fun deleteRow(
        file: String,
        tableName: String,
        row: RowItem
    ): Result<Unit> {
        return safeRequest {
            client.delete("$serverUrl/database/row/$file/$tableName") {
                contentType(ContentType.Application.Json)
                setBody(row)
            }
        }
    }

    override suspend fun executeRawQuery(file: String, query: String): Result<Unit> {
        return safeRequest<Unit> {
            client.post("$serverUrl/ws/db_queries/execute/$file") {
                contentType(ContentType.Text.Plain)
                setBody(query)
            }
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

    @OptIn(FlowPreview::class)
    override fun isConnected(): Flow<Boolean> {
        val maxDelta = 5_000L

        val serverTimeFlow = webSocketFlow<String>("/ws/isAlive") {
            val m = it.substringAfter("ping - ").replace("\"", "")
            m
        }.map {
            when (it) {
                is DataState.Loading<*> -> {
                    System.currentTimeMillis()
                }

                is DataState.Success<*> -> {
                    val time = it.data as String
                    time.toLongOrNull() ?: 0L
                }
            }
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
        }.distinctUntilChanged().debounce(500)
    }


    override fun getEnabledFeatures(): Flow<DataState<EnabledFeathers>> {
        return webSocketFlow("/ws/feathers")
    }


    suspend fun close() {
        client.close()
    }
}
