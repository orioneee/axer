package io.github.orioneee.remote.server

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

    if (updatedOrCreated.isNotEmpty() || deleted.isNotEmpty()) {
        try {
            val totalChunks = maxOf(updatedOrCreated.size, deleted.size)
            repeat(totalChunks) {
                val updatedChunk = updatedOrCreated.getOrNull(it) ?: emptyList()
                val deletedChunk = deleted.getOrNull(it) ?: emptyList()

                sendSerialized(
                    UpdatesData(
                        updatedOrCreated = updatedChunk,
                        deleted = deletedChunk
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
}

@OptIn(FlowPreview::class)
internal inline fun <reified T : Any> Route.reactiveUpdatesSocket(
    path: String,
    crossinline isEnabledFlow: () -> Flow<Boolean>,
    crossinline initialData: suspend () -> List<T>,
    crossinline dataFlow: () -> Flow<List<T>>,
    crossinline getId: (T) -> Long,
    debounceTimeMillis: Long = 300,
    chunkSize: Int = 200
) {
    webSocket(path) {
        val clientState = mutableListOf<T>()



        if (isEnabledFlow().first()) {
            try {
                val all = initialData()
                sendChuncked(
                    path = path,
                    newList = all,
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
            } catch (e: Exception) {
                println("[$path] Initial send failed: ${e.message}")
            }
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
                    try {
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
                    } catch (e: Exception) {
                        println("[$path] Send failed: ${e.message}")
                    }
                }
        }

        for (frame in incoming) {
            // Optionally handle client messages
        }
    }
}
