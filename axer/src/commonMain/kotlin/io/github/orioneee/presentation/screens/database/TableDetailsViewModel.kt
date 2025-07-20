package io.github.orioneee.presentation.screens.database

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.orioneee.AxerDataProvider
import io.github.orioneee.domain.database.EditableRowItem
import io.github.orioneee.domain.database.RoomCell
import io.github.orioneee.domain.database.RowItem
import io.github.orioneee.domain.database.SchemaItem
import io.github.orioneee.domain.database.SortColumn
import io.github.orioneee.extentions.sortBySortingItem
import io.github.orioneee.logger.getPlatformStackTrace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
internal class TableDetailsViewModel(
    private val dataProvider: AxerDataProvider,
    private val file: String,
    private val tableName: String,
) : ViewModel() {
    companion object {
        const val PAGE_SIZE = 20
    }

    private var currentJob: Job? = null


    private val _tableSchema = MutableStateFlow<List<SchemaItem>>(emptyList())
    val tableSchema = _tableSchema.asStateFlow()

    private val _sortColumn = MutableStateFlow<SortColumn?>(null)
    val sortColumn = _sortColumn.asStateFlow()

    private val _currentPage = MutableStateFlow<Int>(0)
    val currentPage = _currentPage.asStateFlow()

    private val _totalTotalItems = MutableStateFlow<Int>(0)
    val totalItems = _totalTotalItems.asStateFlow()

    private val _itemsOnPage = MutableStateFlow<List<List<RoomCell?>>>(emptyList())
    val tableContent = combine(
        _itemsOnPage,
        _sortColumn,
        _tableSchema
    ) { content, sortColumn, schema ->
        if (content.isEmpty() || schema.isEmpty()) {
            return@combine emptyList<RowItem>()
        }
        val rowItems = content.map { row ->
            RowItem(
                cells = row,
                schema = _tableSchema.value
            )
        }
        rowItems.sortBySortingItem(sortColumn)
    }

    private val _isUpdatingCell = MutableStateFlow<Boolean>(false)
    val isUpdatingCell = _isUpdatingCell.asSharedFlow()

    private val _editableRowItem = MutableStateFlow<EditableRowItem?>(null)
    val editableRowItem = _editableRowItem.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    fun updateFlow(){
        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            dataProvider.getDatabaseContent(
                file = file,
                tableName = tableName,
                page = _currentPage.value,
                pageSize = PAGE_SIZE
            ).collect {
                val (schema, content, totalItems) = it
                _tableSchema.value = schema
                _itemsOnPage.value = content
                _totalTotalItems.value = totalItems
            }
        }
    }

    fun clearTable() {
        viewModelScope.launch {
            try {
                _editableRowItem.value = null
                dataProvider.clearTable(
                    file = file,
                    tableName = tableName
                )
            } catch (e: Exception) {
            }
        }
    }

    init {
        updateFlow()
    }

    fun updateCell(
        editableItem: EditableRowItem
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _isUpdatingCell.value = true
            try {
                dataProvider.updateCell(
                    file = file,
                    tableName = tableName,
                    editableItem = editableItem
                )
                _message.value = "Cell updated successfully"
            } catch (e: Exception) {
                throw e
                _message.value = e.getPlatformStackTrace().substringBefore("\n")
            } finally {
                _isUpdatingCell.value = false
                _editableRowItem.value = null
            }
        }
    }

    fun onSelectItem(editableItem: EditableRowItem?) {
        _editableRowItem.value = editableItem
    }

    fun onEditableItemChanged(editableItem: EditableRowItem?) {
        _editableRowItem.value = editableItem
    }

    fun onHandledError() {
        _message.value = null
    }

    fun deleteRow(
        rowItem: RowItem
    ) {
        viewModelScope.launch {
            try {
                _editableRowItem.value?.let {
                    if (it.item == rowItem) {
                        _editableRowItem.value = null
                    }
                }
                dataProvider.deleteRow(
                    file = file,
                    tableName = tableName,
                    row = rowItem
                )
                _message.value = "Row deleted successfully"
            } catch (e: Exception) {
                _message.value = e.getPlatformStackTrace().substringBefore("\n")

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
            val currentSchema = _tableSchema.value
            val index = currentSchema.indexOfFirst { it.name == schemaItem.name }
            _sortColumn.value = SortColumn(
                index = index,
                schemaItem = schemaItem,
                isDescending = true
            )
        }
        updateFlow()
    }

    fun setPage(
        page: Int
    ) {
        if (page != _currentPage.value) {
            _currentPage.value = page
            updateFlow()
        }
    }
}