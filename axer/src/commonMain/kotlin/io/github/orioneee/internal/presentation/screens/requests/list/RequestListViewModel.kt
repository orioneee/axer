package io.github.orioneee.internal.presentation.screens.requests.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.orioneee.internal.AxerDataProvider
import io.github.orioneee.internal.domain.other.DataState
import io.github.orioneee.internal.domain.requests.formatters.BodyType
import io.github.orioneee.internal.extentions.successData
import io.github.orioneee.internal.snackbarProcessor.SnackBarController
import io.github.orioneee.internal.utils.DataExporter
import io.github.orioneee.internal.utils.toHarFile
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

expect fun onClearAllRequests()
internal class RequestListViewModel(
    private val dataProvider: AxerDataProvider
) : ViewModel() {
    private var currentRequestJob: Job? = null

    private val _selectedMethods = MutableStateFlow<List<String>>(emptyList())
    private val _selectedBodyType = MutableStateFlow<List<BodyType>>(emptyList())
    private val _searchQuery: MutableStateFlow<String> = MutableStateFlow("")
    private val _isShowLoadingDialog = MutableStateFlow(false)

    init {
        println("RequestListViewModel initialized")
    }
    @OptIn(FlowPreview::class)
    val isShowLoadingDialog = _isShowLoadingDialog.asStateFlow().debounce(100)
    private val _requestsState by lazy {
        dataProvider.getAllRequests()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(3.seconds),
                initialValue = DataState.Loading()
            )
    }
    val requestsState = _requestsState

    val requests = requestsState.successData().filterNotNull()

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

    val selectedMethods = _selectedMethods.asStateFlow()
    val selectedBodyType = _selectedBodyType.asStateFlow()

    val filteredRequests = combine(
        requests,
        _selectedMethods,
        _selectedBodyType,
        _searchQuery,
    ) { requests, selectedMethods, selectedBodyType, searchQuery ->
        val filteredByMethod = if (selectedMethods.isEmpty()) {
            requests
        } else {
            requests.filter { selectedMethods.contains(it.method) }
        }
        val filteredByType = if (selectedBodyType.isEmpty()) {
            filteredByMethod
        } else {
            filteredByMethod.filter {
                selectedBodyType.contains(it.responseDefaultType)
            }
        }
        filteredByType
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
        currentRequestJob = viewModelScope.launch {
            try {
                _isShowLoadingDialog.value = true
                val res = dataProvider.deleteAllRequests()
                res.onFailure {
                    SnackBarController.showSnackBar(
                        text = "Error clearing requests: ${it.message}",
                    )
                }
                res.onSuccess {
                    onClearAllRequests()
                }
            } finally {
                _selectedMethods.value = emptyList()
                _selectedBodyType.value = emptyList()
                _searchQuery.value = ""
                _isShowLoadingDialog.value = false
            }
        }
    }

    fun exportAsHar() {
        currentRequestJob = viewModelScope.launch {
            try {
                _isShowLoadingDialog.value = true
                dataProvider.getDataForExportAsHar().fold(
                    onSuccess = { harData ->
                        val har = harData.toHarFile()
                        DataExporter.exportHar(har)
                    },
                    onFailure = { error ->
                        SnackBarController.showSnackBar(text = "Error exporting HAR: ${error.message}")
                    }
                )
            } finally {
                _isShowLoadingDialog.value = false
            }
        }
    }

    fun cancelCurrentJob(){
        currentRequestJob?.cancel()
        currentRequestJob = null
        _isShowLoadingDialog.value = false
    }
}