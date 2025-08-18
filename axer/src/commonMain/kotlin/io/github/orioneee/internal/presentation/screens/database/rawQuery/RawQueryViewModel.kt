package io.github.orioneee.internal.presentation.screens.database.rawQuery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.orioneee.internal.AxerDataProvider
import io.github.orioneee.internal.domain.database.QueryResponse
import io.github.orioneee.internal.domain.database.SchemaItem
import io.github.orioneee.internal.domain.database.SortColumn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
internal class RawQueryViewModel(
    private val provider: AxerDataProvider,
    private val name: String,
) : ViewModel() {
    private var currentJob: Job? = null
    private val _currentQuery = MutableStateFlow("")
    val currentQuery = _currentQuery.asStateFlow()

    private val _queryResponse = MutableStateFlow(
        Result.success(
            QueryResponse(
                rows = emptyList(),
                schema = emptyList()
            )
        )
    )
    val queryResponse = _queryResponse.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    init {
        loading
            .onEach {
//                println("Loading state changed: $it")
            }
            .launchIn(viewModelScope)
    }


    private val _sortColumn = MutableStateFlow<SortColumn?>(null)
    val sortColumn = _sortColumn.asStateFlow()

    fun setQuery(query: String) {
        _currentQuery.tryEmit(query)
    }

    fun executeQuery() {
        currentJob?.cancel()
        currentJob = viewModelScope.launch(Dispatchers.IO) {
            val scope = this
            val query = _currentQuery.value
            if (query.isBlank()) return@launch
            else if (!query.startsWith("SELECT", ignoreCase = true)) {
                try {
                    _loading.value = true
                    delay(500.milliseconds)
                    val res = provider.executeRawQuery(
                        file = name,
                        query = query
                    )
                    res.onFailure {
                        _queryResponse.value = Result.failure(it)
//                        SnackBarController.showSnackBar(text = "Error executing query: ${it.message}")
                    }
                    _queryResponse.value = Result.success(
                        QueryResponse(
                            rows = emptyList(),
                            schema = emptyList()
                        )
                    )
                    return@launch
                } finally {
                    _loading.value = false
                }
            } else {
                _loading.value = true
                delay(500.milliseconds)
                _queryResponse.value = Result.success(
                    QueryResponse(
                        rows = emptyList(),
                        schema = emptyList()
                    )
                )
                provider.executeRawQueryAndGetUpdates(
                    file = name,
                    query = query
                ).collect { res ->
                    if (scope.isActive) {
                        _loading.value = false
                        res.onSuccess {
                            _queryResponse.value = Result.success(it)
                        }.onFailure {
                            _queryResponse.value = Result.failure(it)
//                                SnackBarController.showSnackBar(text = text)
                        }
                    }
                }
            }
        }
    }

    fun onClickSortColumn(
        schemaItem: SchemaItem
    ) {
        val currentSort = _sortColumn.value
        if (currentSort != null && currentSort.schemaItem.name == schemaItem.name) {
            _sortColumn.value = currentSort.copy(isDescending = !currentSort.isDescending)
        } else {
            val currentSchema = _queryResponse.value.getOrNull()?.schema ?: return
            val index = currentSchema.indexOfFirst { it.name == schemaItem.name }
            _sortColumn.value = SortColumn(
                index = index,
                schemaItem = schemaItem,
                isDescending = true
            )
        }
    }

    fun cancelCurrentJob() {
        println("Canceling current job")
        currentJob?.cancel()
        currentJob = null
        _loading.value = false
    }
}