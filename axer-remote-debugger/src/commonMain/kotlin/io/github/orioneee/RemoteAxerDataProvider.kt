package io.github.orioneee

import io.github.orioneee.internal.AxerDataProvider
import io.github.orioneee.internal.domain.database.DatabaseData
import io.github.orioneee.internal.domain.database.DatabaseWrapped
import io.github.orioneee.internal.domain.database.EditableRowItem
import io.github.orioneee.internal.domain.database.QueryResponse
import io.github.orioneee.internal.domain.database.RowItem
import io.github.orioneee.internal.domain.exceptions.AxerException
import io.github.orioneee.internal.domain.exceptions.SessionException
import io.github.orioneee.internal.domain.logs.LogLine
import io.github.orioneee.internal.domain.other.AxerServerStatus
import io.github.orioneee.internal.domain.other.BaseResponse
import io.github.orioneee.internal.domain.other.DataState
import io.github.orioneee.internal.domain.other.EnabledFeathers
import io.github.orioneee.internal.domain.requests.data.TransactionFull
import io.github.orioneee.internal.domain.requests.data.TransactionShort
import io.github.orioneee.internal.remote.server.UpdatesData
import io.github.orioneee.internal.remote.server.requestReplaceAll
import io.github.orioneee.internal.remote.server.toSha256Hash
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
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.net.URI
import kotlin.time.Duration.Companion.seconds

class RemoteAxerDataProvider(
    private val serverUrl: String,
) : AxerDataProvider {
    val myJson = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
    }
    private val client = HttpClient {
        install(WebSockets.Plugin) {
            contentConverter = KotlinxWebsocketSerializationConverter(
                myJson
            )
        }
        install(ContentNegotiation) {
            json(myJson)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private inline fun <reified T> webSocketFlowWithSendChannel(
        path: String,
        crossinline dataMapper: (T) -> T = { it },
    ): Pair<Flow<DataState<T>>, SendChannel<String>> {
        val sendChannel = Channel<String>(Channel.UNLIMITED)
        val flow = callbackFlow {
            val uri = URI(serverUrl)
            trySend(DataState.Loading())

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
                            val outgoingJob = launch {
                                for (msg in sendChannel) {
                                    println("Sending message: $msg to path: $path")
                                    send(Frame.Text(msg))
                                }
                            }

                            try {
                                for (frame in incomingFrames) {
                                    if (frame is Frame.Text) {
                                        val data = frame.readText()
                                        val sizeInBytes = data.toByteArray().size
                                        val obj = myJson.decodeFromString<T>(data)
                                        val mapped = dataMapper(obj)
                                        trySend(DataState.Success(mapped)).isSuccess
                                    }
                                }
                            } catch (e: Exception) {
                                throw e
                            } finally {
                                outgoingJob.cancel()
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
                sendChannel.close()
            }
        }.distinctUntilChanged()

        return Pair(flow, sendChannel)
    }


    @OptIn(DelicateCoroutinesApi::class)
    private inline fun <reified T> webSocketFlow(
        path: String,
        crossinline dataMapper: (T) -> T = { it },
    ): Flow<DataState<T>> = webSocketFlowWithSendChannel(
        path = path,
        dataMapper = dataMapper
    ).first

    private inline fun <reified T : Any> webSocketUpdatesFlow(
        path: String,
        crossinline getId: (T) -> Long,
        crossinline sorter: (List<T>) -> List<T>
    ): Flow<DataState<List<T>>> {
        val currentState = mutableListOf<T>()

        val data = webSocketFlowWithSendChannel<UpdatesData<T>>(path)
        val mapped = data.first.map { state ->
            when (state) {
                is DataState.Loading -> {
                    DataState.Loading()
                }

                is DataState.Success<*> -> {
                    val update = state.data as UpdatesData<T>
                    if (update.replaceWith.isEmpty() && !update.replaceAll) {
                        currentState.removeAll { oldItem ->
                            update.deleted.contains(getId(oldItem))
                        }

                        update.updatedOrCreated.forEach { newItem ->
                            val index =
                                currentState.indexOfFirst { getId(it) == getId(newItem) }
                            if (index != -1) {
                                currentState[index] = newItem
                            } else {
                                currentState.add(newItem)
                            }
                        }
                        val hashFromServer = update.hash
                        val currentHash = currentState.toSha256Hash{
                            getId(it)
                        }
                        if (hashFromServer != currentHash) {
                            println("Hash mismatch! Server: $hashFromServer, Client: $currentHash")
                            val message = Frame.requestReplaceAll
                            val result = data.second.trySend(message)
                            if (result.isFailure) {
                                println("Failed to send replaceAll request frame. Reason: ${result.exceptionOrNull()}")
                            }

                        } else{
                            println("Hash match! Server: $hashFromServer, Client: $currentHash")
                        }
                    } else {
                        println("Replacing all items with new list of size: ${update.replaceWith.size}")
                        currentState.clear()
                        currentState.addAll(update.replaceWith)
                    }
                    DataState.Success(sorter(currentState).toList())
                }
            }
        }.distinctUntilChanged()

        return mapped
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
            val textBody = response.bodyAsText()
            val resp = myJson.decodeFromString<BaseResponse<T>>(textBody)
            resp.toResult()
        } catch (e: Exception) {
            e.printStackTrace()
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
        return safeRequest<String> {
            client.post("$serverUrl/requests/view/$id")
        }.fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { Result.failure(it) }
        )
    }

    override suspend fun deleteAllRequests(): Result<String> {
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

    override suspend fun getSessionEventsByException(id: Long): Result<SessionException> {
        return safeRequest {
            client.get("$serverUrl/exceptions/$id")
        }
    }


    override suspend fun deleteAllExceptions(): Result<Unit> {
        return safeRequest<String> {
            client.delete("$serverUrl/exceptions")
        }.fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { Result.failure(it) }
        )
    }

    override fun getAllLogs(): Flow<DataState<List<LogLine>>> =
        webSocketUpdatesFlow(
            path = "/ws/logs",
            getId = { it.id },
            sorter = { it.sortedByDescending { it.time } }
        )


    override suspend fun deleteAllLogs(): Result<Unit> {
        return safeRequest<String> {
            client.delete("$serverUrl/logs")
        }.fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { Result.failure(it) }
        )
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

    override suspend fun clearTable(file: String, tableName: String): Result<String> {
       return safeRequest<String> {
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
        return safeRequest<String> {
            client.post("$serverUrl/database/cell/$file/$tableName") {
                contentType(ContentType.Application.Json)
                setBody(editableItem)
            }
        }.fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { Result.failure(it) }
        )
    }

    override suspend fun deleteRow(
        file: String,
        tableName: String,
        row: RowItem
    ): Result<Unit> {
        return safeRequest<String> {
            client.delete("$serverUrl/database/row/$file/$tableName") {
                contentType(ContentType.Application.Json)
                setBody(row)
            }
        }.fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { Result.failure(it) }
        )
    }

    override suspend fun executeRawQuery(file: String, query: String): Result<Unit> {
        return safeRequest<String> {
            client.post("$serverUrl/ws/db_queries/execute/$file") {
                contentType(ContentType.Text.Plain)
                setBody(query)
            }
        }.fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { Result.failure(it) }
        )
    }


    override fun executeRawQueryAndGetUpdates(
        file: String,
        query: String
    ): Flow<Result<QueryResponse>> = callbackFlow {
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
                                val response = myJson.decodeFromString<BaseResponse<QueryResponse>>(text)
                                trySend(response.toResult()).isSuccess
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

    data class ConnectionState(
        val isConnected: Boolean,
        val rtt: Long
    )

    @OptIn(DelicateCoroutinesApi::class)
    fun connectionFlow(): StateFlow<ConnectionState> = callbackFlow {
        val uri = URI(serverUrl)

        val job = launch {
            while (isActive) {
                try {
                    client.webSocket(
                        method = HttpMethod.Get,
                        host = uri.host,
                        port = uri.port,
                        path = "/ws/echo"
                    ) {
                        val sender = launch {
                            while (isActive) {
                                val sentTime = System.currentTimeMillis()
                                val msg = """{"type":"ping","sent":$sentTime}"""
                                send(Frame.Text(msg))
                                delay(1.seconds)
                            }
                        }

                        for (frame in incoming) {
                            if (frame is Frame.Text) {
                                val text = frame.readText()
                                val sentTime = Regex(""""sent":(\d+)""")
                                    .find(text)
                                    ?.groupValues?.get(1)?.toLongOrNull()

                                if (sentTime != null) {
                                    val rtt = System.currentTimeMillis() - sentTime
                                    trySend(ConnectionState(true, rtt)).isSuccess
                                }
                            }
                        }

                        sender.cancel()
                    }

                    delay(1_000)

                } catch (e: Exception) {
                    if (e.message?.contains("404") == true) {
                        println("connectionFlow: server returned 404, stopping reconnects")
                        break
                    } else {
                        println("connectionFlow error: ${e.message}, retrying in 2s")
                        trySend(ConnectionState(false, 0L)).isSuccess
                        delay(2_000)
                    }
                }
            }
        }

        awaitClose { job.cancel() }
    }.stateIn(
        scope = GlobalScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ConnectionState(true, 0L)
    )



    val connection = connectionFlow()

    @OptIn(FlowPreview::class)
    val connectedFlow: Flow<Boolean> = connection.map { it.isConnected }.debounce(500)
    val pingFlow: Flow<Long> = connection.map { it.rtt }.distinctUntilChanged().sample(1.seconds)

    @OptIn(FlowPreview::class, DelicateCoroutinesApi::class)
    override fun isConnected(): Flow<Boolean> = connectedFlow

    @OptIn(DelicateCoroutinesApi::class)
    fun realPingFlow(): StateFlow<Long> = callbackFlow {
        val uri = URI(serverUrl)

        val job = launch {
            while (isActive) {
                try {
                    client.webSocket(
                        method = HttpMethod.Get,
                        host = uri.host,
                        port = uri.port,
                        path = "/ws/echo"
                    ) {
                        // sender job: send pings every second
                        val sender = launch {
                            while (isActive) {
                                val sentTime = System.currentTimeMillis()
                                val msg = """{"type":"ping","sent":$sentTime}"""
                                send(Frame.Text(msg))
                                delay(1.seconds)
                            }
                        }

                        // receive echoed frames
                        for (frame in incoming) {
                            if (frame is Frame.Text) {
                                val text = frame.readText()
                                val sentTime = Regex(""""sent":(\d+)""")
                                    .find(text)
                                    ?.groupValues?.get(1)?.toLongOrNull()

                                if (sentTime != null) {
                                    val rtt = System.currentTimeMillis() - sentTime
                                    trySend(rtt).isSuccess
                                }
                            }
                        }

                        sender.cancel()
                    }

                    // if socket closes normally, reconnect after short delay
                    delay(1_000)
                } catch (e: Exception) {
                    // Check for 404
                    if (e.message?.contains("404") == true) {
                        println("realPingFlow: server returned 404, stopping reconnects")
                        break
                    } else {
                        println("realPingFlow error: ${e.message}, retrying in 2s")
                        delay(2_000)
                    }
                }
            }
        }

        awaitClose { job.cancel() }
    }.stateIn(
        scope = GlobalScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0L
    )



    val enabledFeathersFlow = webSocketFlow<EnabledFeathers>("/ws/feathers")
        .stateIn(
            scope = GlobalScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DataState.Loading()
        )


    @OptIn(DelicateCoroutinesApi::class)
    override fun getEnabledFeatures(): Flow<DataState<EnabledFeathers>> = enabledFeathersFlow


    override fun isServerRunning(): Flow<AxerServerStatus> = flowOf(AxerServerStatus.NotSupported)


    suspend fun close() {
        client.close()
    }
}
