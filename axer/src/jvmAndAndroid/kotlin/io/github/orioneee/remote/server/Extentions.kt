package io.github.orioneee.remote.server

import androidx.compose.ui.util.fastCoerceAtLeast
import io.ktor.server.routing.Route
import io.ktor.server.websocket.sendSerialized
import io.ktor.server.websocket.webSocket
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.collections.chunked


private suspend inline fun <T> sendChuncked(
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
                    replaceAll = false
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

@OptIn(FlowPreview::class)
internal inline fun <reified T : Any> Route.reactiveUpdatesSocket(
    path: String,
    crossinline isEnabledFlow: () -> Flow<Boolean>,
    crossinline initialData: suspend () -> List<T>,
    crossinline dataFlow: () -> Flow<List<T>>,
    crossinline getId: (T) -> Long,
    debounceTimeMillis: Long = 300,
    chunkSize: Int = 200,
    sendsToReplaceAll: Int = 10
) {
    var leftToReplaceAll = sendsToReplaceAll
    webSocket(path) {
        val clientState = mutableListOf<T>()

        if (isEnabledFlow().first()) {
            try {
                val all = initialData()
                sendSerialized(
                    UpdatesData(
                        updatedOrCreated = emptyList(),
                        deleted = emptyList(),
                        replaceWith = all,
                        replaceAll = true
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

        launch {
            combine(
                dataFlow(), isEnabledFlow()
            ) { data, isEnabled ->
                isEnabled to data
            }
                .distinctUntilChanged()
                .debounce(debounceTimeMillis)
                .collect { (isEnabled, newList) ->
                    ensureActive()
                    if (!isEnabled) return@collect
                    if (leftToReplaceAll > 0) {
                        leftToReplaceAll--
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
                    } else{
                        println("[$path] Replacing all data with new list of size ${newList.size}")
                        sendSerialized(
                            UpdatesData(
                                updatedOrCreated = emptyList(),
                                deleted = emptyList(),
                                replaceWith = newList,
                                replaceAll = true
                            )
                        )
                        leftToReplaceAll = sendsToReplaceAll
                    }
                }
        }

        for (frame in incoming) {
            // Optionally handle client messages
        }
    }
}
