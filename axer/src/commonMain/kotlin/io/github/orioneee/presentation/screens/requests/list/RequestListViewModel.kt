package io.github.orioneee.presentation.screens.requests.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.orioneee.domain.requests.formatters.BodyType
import io.github.orioneee.room.dao.RequestDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

internal class RequestListViewModel(
    private val requestDao: RequestDao,
) : ViewModel() {


    val requests = requestDao.getAll()

    val methodFilters = requests.map {
        it.map { it.method }
            .distinct()
            .sortedBy { it }
            .takeIf { it.size > 1 } ?: emptyList()
    }
    val bodyTypeFilters = requests.map {
        it.map { it.responseDefaultType }
            .distinct()
            .sortedBy { it }
            .filterNotNull()
            .takeIf { it.size > 1 }
            ?: emptyList()
    }


    private val _selectedMethods = MutableStateFlow<List<String>>(emptyList())
    private val _selectedBodyType = MutableStateFlow<List<BodyType>>(emptyList())

    val selectedMethods = _selectedMethods.asStateFlow()
    val selectedBodyType = _selectedBodyType.asStateFlow()

    val filteredRequests = combine(
        requests,
        _selectedMethods,
        selectedBodyType,
    ) { requests, selectedMethods, selectedBodyType ->
        val filterdByMethod = if (selectedMethods.isEmpty()) {
            requests
        } else {
            requests.filter { selectedMethods.contains(it.method) }
        }
        val filterdByType = if (selectedBodyType.isEmpty()) {
            filterdByMethod
        } else {
            filterdByMethod.filter {
                selectedBodyType.contains(it.responseDefaultType)
            }
        }
        filterdByType
    }


    fun toggleMethodFilter(method: String) {
        viewModelScope.launch {
            _selectedMethods.value = if (_selectedMethods.value.contains(method)) {
                _selectedMethods.value - method
            } else {
                _selectedMethods.value + method
            }
        }
    }

    fun toggleTypeFilter(filter: BodyType) {
        viewModelScope.launch {
            _selectedBodyType.value = if (_selectedBodyType.value.contains(filter)) {
                _selectedBodyType.value - filter
            } else {
                _selectedBodyType.value + filter
            }
        }
    }


    fun clearMethodFilters() {
        viewModelScope.launch {
            _selectedMethods.value = emptyList()
        }
    }

    fun clearTypeFilters() {
        viewModelScope.launch {
            _selectedBodyType.value = emptyList()
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            requestDao.deleteAll()
        }
    }


}