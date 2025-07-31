package io.github.orioneee.remote.server

import androidx.compose.ui.util.fastCoerceAtLeast
import io.ktor.server.routing.Route
import io.ktor.server.websocket.sendSerialized
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import kotlin.collections.chunked


private suspend inline fun <reified T> sendChuncked(
    path: String,
    newList: List<T>,
    clientState: List<T>,
    crossinline getId: (T) -> Long,
    chunkSize: Int,
    crossinline sendSerialized: suspend (UpdatesData<T>) -> Unit,
    crossinline onRemove: (List<Long>) -> Unit,
    crossinline onAdd: (List<T>) -> Unit
) {
    val updatedOrCreated = newList.filter { it !in clientState }.chunked(chunkSize)
    val deleted =
        (clientState.map { getId(it) } - newList.map { getId(it) }).chunked(chunkSize)

    try {
        val totalChunks = maxOf(updatedOrCreated.size, deleted.size).fastCoerceAtLeast(1)
        println("[$path] Sending ${updatedOrCreated.size} updated/created chunks and ${deleted.size} deleted chunks, total: $totalChunks")
        repeat(totalChunks) {
            val updatedChunk = updatedOrCreated.getOrNull(it) ?: emptyList()
            val deletedChunk = deleted.getOrNull(it) ?: emptyList()

            sendSerialized(
                UpdatesData(
                    updatedOrCreated = updatedChunk,
                    deleted = deletedChunk,
                    replaceWith = emptyList(),
                    replaceAll = false,
                    hash = newList.toSha256Hash{
                        getId(it)
                    }
                )
            )

            val removed = clientState.filter { getId(it) in deletedChunk }.map { getId(it) }
            onRemove(removed)
            val added = updatedChunk.filter { it !in clientState }
            onAdd(added)
        }
    } catch (e: Exception) {
        println("[$path] Send failed: ${e.message}")
    }
}


@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T> List<T>.toSha256Hash(
    crossinline getID: (T) -> Long = { it.hashCode().toLong() }
): String {
    val json = Json { encodeDefaults = true }

    val byteStream = ByteArrayOutputStream()
    json.encodeToStream(this.sortedBy { getID(it) }, byteStream)
    val bytes = byteStream.toByteArray()

    val digest = MessageDigest.getInstance("SHA-256")
    val hash = digest.digest(bytes)

    return hash.joinToString("") { "%02x".format(it) }
}

@OptIn(FlowPreview::class)
internal inline fun <reified T : Any> Route.reactiveUpdatesSocket(
    path: String,
    crossinline isEnabledFlow: () -> Flow<Boolean>,
    crossinline initialData: suspend () -> List<T>,
    crossinline dataFlow: () -> Flow<List<T>>,
    crossinline getId: (T) -> Long,
    chunkSize: Int = 200
) {
    val mutex = Mutex()
    webSocket(path) {
        val clientState = mutableListOf<T>()

        mutex.withLock {
            if (isEnabledFlow().first()) {
                try {
                    val all = initialData()
                    sendSerialized(
                        UpdatesData(
                            updatedOrCreated = emptyList(),
                            deleted = emptyList(),
                            replaceWith = all,
                            replaceAll = true,
                            hash = all.toSha256Hash(getId)
                        )
                    )
                    clientState.addAll(all)
                } catch (e: Exception) {
                    println("[$path] Initial send failed: ${e.message}")
                    throw e
                }
            } else {
                sendSerialized(null)
            }
        }

        launch {
            combine(
                dataFlow(), isEnabledFlow()
            ) { data, isEnabled ->
                isEnabled to data
            }
                .distinctUntilChanged()
                .collect { (isEnabled, newList) ->
                    mutex.withLock {
                        ensureActive()
                        if (!isEnabled) return@collect
                        sendChuncked(
                            path = path,
                            newList = newList,
                            clientState = clientState,
                            getId = getId,
                            chunkSize = chunkSize,
                            sendSerialized = ::sendSerialized,
                            onRemove = { removedIds ->
                                clientState.removeAll { getId(it) in removedIds }
                            },
                            onAdd = { addedItems ->
                                clientState.addAll(addedItems)
                            }
                        )
                    }
                }
        }

        for (frame in incoming) {
            if (frame is Frame.Text){
                if (frame.readText() == Frame.requestReplaceAll) {
                    println("[$path] Received request to replace all data")
                    mutex.withLock {
                        ensureActive()
                        val list = dataFlow().first()
                        clientState.clear()
                        clientState.addAll(list)
                        sendSerialized(
                            UpdatesData(
                                updatedOrCreated = emptyList(),
                                deleted = emptyList(),
                                replaceWith = list,
                                replaceAll = true,
                                hash = list.toSha256Hash{
                                    getId(it)
                                }
                            )
                        )
                    }
                }
            }
        }
    }
}

val Frame.Companion.requestReplaceAll: String
    get() = "replaceAll"
