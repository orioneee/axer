package com.oriooneee.axer.presentation.screens.database

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oriooneee.axer.domain.database.RowItem
import com.oriooneee.axer.domain.database.SchemaItem
import com.oriooneee.axer.room.AxerBundledSQLiteDriver
import com.oriooneee.axer.room.RoomReader
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class DatabaseInspectionViewModel(
    private val tableName: String?,
) : ViewModel() {
    private val reader = RoomReader(AxerBundledSQLiteDriver.instance)

    private val _tables = MutableStateFlow<List<String>>(emptyList())
    val tables = _tables.asStateFlow()

    private val _tableSchema = MutableStateFlow<List<SchemaItem>>(emptyList())
    val tableSchema = _tableSchema.asStateFlow()

    private val _tableContent = MutableStateFlow<List<RowItem>>(emptyList())
    val tableContent = _tableContent.asStateFlow()

    fun loadTables() {
        viewModelScope.launch {
            try {
                val tableList = reader.getAllTables()
                _tables.value = tableList
            } catch (e: Exception) {
                e.printStackTrace()
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
                        e.printStackTrace()
                        null
                    }
                }
                val content = async {
                    try {
                        val content = reader.getTableContent(tableName)
//                        _tableContent.value = content
                        content
                    } catch (e: Exception) {
                        e.printStackTrace()
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
                reader.clearTable(tableName ?: "")
                getTableInfo()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    init {
        reader.axerDriver.queryFlow
            .debounce(100)
            .onEach {
                if (tableName != null) {
                    getTableInfo()
                }
            }
            .launchIn(viewModelScope)
    }
}