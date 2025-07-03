package io.github.orioneee.presentation.screens.database.tableList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.orioneee.Axer
import io.github.orioneee.domain.database.DatabaseWrapped
import io.github.orioneee.processors.RoomReader
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
internal class ListDatabaseViewModel : ViewModel() {
    private val reader = RoomReader()

    private val _databases = MutableStateFlow<List<DatabaseWrapped>>(emptyList())

    val databases = _databases.asStateFlow()


    fun loadDatabases() {
        viewModelScope.launch {
            try {
                val tableList = reader.getTablesFromAllDatabase()
                _databases.value = tableList
            } catch (e: Exception) {
                _databases.value = emptyList()
                Axer.e("ListDatabaseViewModel", "Error loading databases: ${e.message}", e)
            }
        }
    }

    init {
        loadDatabases()
        reader.axerDriver.changeDataFlow
            .debounce(100)
            .onEach {
                Axer.d("ListDatabaseViewModel", "Database change detected: $it", record = false)
                loadDatabases()
            }
            .launchIn(viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
        reader.release()
    }
}