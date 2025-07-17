package io.github.orioneee.presentation.screens.database.rawQuery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.orioneee.AxerDataProvider
import io.github.orioneee.domain.database.QueryResponse
import io.github.orioneee.domain.database.SchemaItem
import io.github.orioneee.domain.database.SortColumn
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
                _isLoading.value = true
                provider.executeRawQuery(
                    file = name,
                    query = query
                )
                _queryResponse.value = QueryResponse(
                    rows = emptyList(),
                    schema = emptyList()
                )
                _isLoading.value = false
                return@launch
            } else {
                _queryResponse.value = QueryResponse(
                    rows = emptyList(),
                    schema = emptyList()
                )
                provider.excecuteRawQueryAndGetUpdates(
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
}