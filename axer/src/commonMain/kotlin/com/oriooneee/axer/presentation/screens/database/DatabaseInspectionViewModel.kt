package com.oriooneee.axer.presentation.screens.database

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oriooneee.axer.domain.database.EditableRowItem
import com.oriooneee.axer.domain.database.RowItem
import com.oriooneee.axer.domain.database.SchemaItem
import com.oriooneee.axer.domain.database.SortColumn
import com.oriooneee.axer.domain.database.Table
import com.oriooneee.axer.getPlatformStackTrace
import com.oriooneee.axer.room.AxerBundledSQLiteDriver
import com.oriooneee.axer.room.RoomReader
import com.oriooneee.axer.sortBySortingItemAndChunck
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
internal class DatabaseInspectionViewModel(
    private val tableName: String?,
) : ViewModel() {
    companion object {
        const val PAGE_SIZE = 20
    }

    private val reader = RoomReader(AxerBundledSQLiteDriver.instance)

    private val _tables = MutableStateFlow<List<Table>>(emptyList())
    val tables = _tables.asStateFlow()

    private val _tableSchema = MutableStateFlow<List<SchemaItem>>(emptyList())
    val tableSchema = _tableSchema.asStateFlow()

    private val _sortColumn = MutableStateFlow<SortColumn?>(null)
    val sortColumn = _sortColumn.asStateFlow()

    private val _tableContent = MutableStateFlow<List<RowItem>>(emptyList())
    val tableContent = combine(
        _tableContent,
        _sortColumn
    ) { content, sortColumn ->
        content.sortBySortingItemAndChunck(sortColumn)
    }

    private val _isUpdatingCell = MutableStateFlow<Boolean>(false)
    val isUpdatingCell = _isUpdatingCell.asSharedFlow()

    private val _editableRowItem = MutableStateFlow<EditableRowItem?>(null)
    val editableRowItem = _editableRowItem.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()


    fun loadTables() {
        viewModelScope.launch {
            try {
                val tableList = reader.getAllTables()
                _tables.value = tableList
            } catch (e: Exception) {
                _tables.value = emptyList()
            }
        }
    }

    fun getTableInfo() {
        viewModelScope.launch {
            if (tableName != null) {
                val schema = async {
                    try {
                        val schema = reader.getTableSchema(tableName)
                        _tableSchema.value = schema
                        schema
                    } catch (e: Exception) {
                        null
                    }
                }
                val content = async {
                    try {
                        val content = reader.getTableContent(tableName)
//                        _tableContent.value = content
                        content
                    } catch (e: Exception) {
                        null
                    }
                }
                val schemaResult = schema.await()
                val contentResult = content.await()
                val rowItems = contentResult?.map { row ->
                    RowItem(
                        schema = schemaResult ?: emptyList(),
                        cells = row
                    )
                } ?: emptyList()
                _tableContent.value = rowItems
            }
        }
    }

    fun clearTable() {
        viewModelScope.launch {
            try {
                _editableRowItem.value = null
                reader.clearTable(tableName ?: "")
                getTableInfo()
            } catch (e: Exception) {
            }
        }
    }

    init {
        reader.axerDriver.queryFlow
            .debounce(100)
            .onEach {
                if (tableName == null) {
                    loadTables()
                } else {
                    getTableInfo()
                }
            }
            .launchIn(viewModelScope)
    }

    fun updateCell(
        editableItem: EditableRowItem
    ) {
        if (tableName == null) return
        viewModelScope.launch(Dispatchers.IO) {
            _isUpdatingCell.value = true
            try {
                reader.updateCell(
                    tableName = tableName,
                    editableItem = editableItem
                )
                getTableInfo()
                _message.value = "Cell updated successfully"
            } catch (e: Exception) {
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
        if (tableName == null) return
        viewModelScope.launch {
            try {
                _editableRowItem.value?.let {
                    if (it.item == rowItem) {
                        _editableRowItem.value = null
                    }
                }
                reader.deleteRow(
                    tableName,
                    row = rowItem
                )
                getTableInfo()
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
    }
}