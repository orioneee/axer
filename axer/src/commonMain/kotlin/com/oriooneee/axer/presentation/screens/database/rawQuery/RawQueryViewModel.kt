package com.oriooneee.axer.presentation.screens.database.rawQuery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oriooneee.axer.domain.database.QueryResponse
import com.oriooneee.axer.domain.database.SchemaItem
import com.oriooneee.axer.domain.database.SortColumn
import com.oriooneee.axer.room.AxerBundledSQLiteDriver
import com.oriooneee.axer.room.RoomReader
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
internal class RawQueryViewModel : ViewModel() {
    private val reader = RoomReader(AxerBundledSQLiteDriver.instance)

    private val _currentQuery = MutableStateFlow("")
    val currentQuery = _currentQuery

    private val _queryResponse = MutableStateFlow<QueryResponse>(
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
        viewModelScope.launch {
            _isLoading.value = true
            _queryResponse.value = QueryResponse(
                rows = emptyList(),
                schema = emptyList()
            )
            try {
                val response = reader.executeRawQuery(_currentQuery.value)
                _queryResponse.value = response
            } catch (e: Exception) {
            } finally {
                _isLoading.value = false
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

    init {
        reader.axerDriver.queryFlow
            .debounce(100)
            .onEach {
                executeQuery()
            }
            .launchIn(viewModelScope)
    }

}