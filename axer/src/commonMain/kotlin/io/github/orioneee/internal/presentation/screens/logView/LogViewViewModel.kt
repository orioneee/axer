package io.github.orioneee.internal.presentation.screens.logView

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.aakira.napier.LogLevel
import io.github.orioneee.internal.AxerDataProvider
import io.github.orioneee.internal.extentions.successData
import io.github.orioneee.internal.snackbarProcessor.SnackBarController
import io.github.orioneee.internal.utils.DataExporter
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

internal class LogViewViewModel(
    private val dataProvider: AxerDataProvider,
) : ViewModel() {
    private var currentJob: Job? = null
    private val _selectedTags = MutableStateFlow<List<String>>(listOf())
    private val _selectedLevels = MutableStateFlow<List<LogLevel>>(listOf())
    private val _isExporting = MutableStateFlow(false)
    private val _firstExportPointId = MutableStateFlow<Long?>(null)
    private val _lastExportPointId = MutableStateFlow<Long?>(null)
    private val _isShowLoadingDialog = MutableStateFlow(false)

    val selectedTags = _selectedTags.asStateFlow()
    val selectedLevels = _selectedLevels.asStateFlow()
    val isExporting = _isExporting.asStateFlow()
    val firstExportPointId = _firstExportPointId.asStateFlow()
    val lastExportPointId = _lastExportPointId.asStateFlow()
    @OptIn(FlowPreview::class)
    val isShowLoadingDialog = _isShowLoadingDialog.asStateFlow().debounce(100)

    @OptIn(FlowPreview::class)
    val logsState by lazy {
        dataProvider.getAllLogs().shareIn(viewModelScope , SharingStarted.WhileSubscribed(), replay = 1)
    }
    val logs = logsState.successData().filterNotNull()
    val filtredLogs = combine(
        logs,
        _selectedTags,
        _selectedLevels
    ) { logs, selectedTags, selectedLevels ->
        logs.filter { log ->
            (selectedTags.isEmpty() || log.tag in selectedTags) &&
                    (selectedLevels.isEmpty() || log.level in selectedLevels)
        }
    }
    val selectedForExport = combine(
        filtredLogs,
        _firstExportPointId,
        _lastExportPointId
    ) { logs, firstExportPointId, lastExportPointId ->
        if (firstExportPointId == null || lastExportPointId == null) return@combine emptyList()
        val isPresentFirst = logs.any { it.id == firstExportPointId }
        val isPresentLast = logs.any { it.id == lastExportPointId }
        if (!isPresentFirst) {
            _firstExportPointId.value = null
        }
        if (!isPresentLast) {
            _lastExportPointId.value = null
        }
        if (!isPresentFirst || !isPresentLast) return@combine emptyList()
        val indexOfFirst = logs.indexOfFirst { it.id == firstExportPointId }.takeIf {
            it >= 0
        } ?: return@combine emptyList()
        val indexOfLast = logs.indexOfLast { it.id == lastExportPointId }.takeIf {
            it >= 0
        } ?: return@combine emptyList()
        logs.subList(
            fromIndex = min(indexOfFirst, indexOfLast),
            toIndex = (max(indexOfFirst, indexOfLast) + 1).coerceAtMost(logs.size)
        )
    }
    val tags = logs.map {
        it.mapNotNull { log -> log.tag }
            .distinct()
            .sortedBy { it }
    }
    val levels = logs.map {
        it.map { log -> log.level }
            .distinct()
            .sortedBy { it.ordinal }
    }

    fun toggleTag(tag: String) {
        _selectedTags.value = if (_selectedTags.value.contains(tag)) {
            _selectedTags.value - tag
        } else {
            _selectedTags.value + tag
        }
    }

    fun toggleLevel(level: LogLevel) {
        _selectedLevels.value = if (_selectedLevels.value.contains(level)) {
            _selectedLevels.value - level
        } else {
            _selectedLevels.value + level
        }
    }

    fun clearTags() {
        _selectedTags.value = listOf()
    }

    fun clearLevels() {
        _selectedLevels.value = listOf()
    }

    fun clear() {
        currentJob = viewModelScope.launch {
            try {
                _isShowLoadingDialog.value = true
                val res = dataProvider.deleteAllLogs()
                res.onFailure {
                    SnackBarController.showSnackBar(text = "Failed to clear logs: ${it.message}")
                }
            } finally {
                _isShowLoadingDialog.value = false
            }
        }
    }

    fun onClickExport() {
        _isExporting.value = !_isExporting.value
        if (!_isExporting.value) {
            _firstExportPointId.value = null
            _lastExportPointId.value = null
        }
    }

    fun onSelectPoint(id: Long) {
        if (_firstExportPointId.value == id && _lastExportPointId.value == null) {
            return
        } else if (_lastExportPointId.value == id && _firstExportPointId.value == null) {
            return
        } else if (_firstExportPointId.value == null) {
            _firstExportPointId.value = id
        } else if (_lastExportPointId.value == null) {
            _lastExportPointId.value = id
        } else {
            _firstExportPointId.value = id
            _lastExportPointId.value = null
        }
    }

    fun onExport() {
        currentJob = viewModelScope.launch {
            try {
                _isExporting.value = true
                val logs = selectedForExport.first()
                DataExporter.exportLogs(logs)
            } catch (e: Exception) {
                // Handle export error, e.g., show a message to the user
            } finally {
                _isShowLoadingDialog.value = false
                _isExporting.value = false
                _firstExportPointId.value = null
                _lastExportPointId.value = null
            }
        }
    }

    fun cancelCurrentJob() {
        currentJob?.cancel()
        currentJob = null
        _isShowLoadingDialog.value = false
    }
}