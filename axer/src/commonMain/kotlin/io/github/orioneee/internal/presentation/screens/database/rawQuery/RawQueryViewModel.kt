package io.github.orioneee.internal.presentation.screens.database.rawQuery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.orioneee.internal.AxerDataProvider
import io.github.orioneee.internal.domain.database.QueryResponse
import io.github.orioneee.internal.domain.database.SchemaItem
import io.github.orioneee.internal.domain.database.SortColumn
import io.github.orioneee.internal.snackbarProcessor.SnackBarController
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
internal class RawQueryViewModel(
    private val provider: AxerDataProvider,
    private val name: String,
) : ViewModel() {
    private var currentJob: Job? = null
    private val _currentQuery = MutableStateFlow("")
    val currentQuery = _currentQuery.asStateFlow()

    private val _queryResponse = MutableStateFlow(
        QueryResponse(
            rows = emptyList(),
            schema = emptyList()
        )
    )
    val queryResponse = _queryResponse.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asSharedFlow()

    private val _sortColumn = MutableStateFlow<SortColumn?>(null)
    val sortColumn = _sortColumn.asStateFlow()

    fun setQuery(query: String) {
        _currentQuery.tryEmit(query)
    }

    fun executeQuery() {
        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            val query = _currentQuery.value
            if (query.isBlank()) return@launch
            else if (!query.startsWith("SELECT", ignoreCase = true)) {
                try {
                    _isLoading.value = true
                    val res = provider.executeRawQuery(
                        file = name,
                        query = query
                    )
                    res.onFailure {
                        SnackBarController.showSnackBar(text = "Error executing query: ${it.message}")
                    }
                    _queryResponse.value = QueryResponse(
                        rows = emptyList(),
                        schema = emptyList()
                    )
                    return@launch
                } finally {
                    _isLoading.value = false
                }
            } else {
                _queryResponse.value = QueryResponse(
                    rows = emptyList(),
                    schema = emptyList()
                )
                provider.executeRawQueryAndGetUpdates(
                    file = name,
                    query = query
                ).collect { response ->
                    _queryResponse.value = response
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
            val currentSchema = _queryResponse.value.schema
            val index = currentSchema.indexOfFirst { it.name == schemaItem.name }
            _sortColumn.value = SortColumn(
                index = index,
                schemaItem = schemaItem,
                isDescending = true
            )
        }
    }

    fun cancelCurrentJob() {
        currentJob?.cancel()
        currentJob = null
        _isLoading.value = false
    }
}