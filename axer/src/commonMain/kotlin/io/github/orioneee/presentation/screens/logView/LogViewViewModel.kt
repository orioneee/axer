package io.github.orioneee.presentation.screens.logView

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.aakira.napier.LogLevel
import io.github.orioneee.koin.IsolatedContext
import io.github.orioneee.room.dao.LogsDAO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

internal class LogViewViewModel : ViewModel() {
    private val dao: LogsDAO by IsolatedContext.koin.inject()

    private val _selectedTags = MutableStateFlow<List<String>>(listOf())
    private val _selectedLevels = MutableStateFlow<List<LogLevel>>(listOf())

    val selectedTags = _selectedTags.asStateFlow()
    val selectedLevels = _selectedLevels.asStateFlow()


    val logs = dao.getAll().map { it.reversed() }
    val filtredLogs = combine(
        logs,
        _selectedTags,
        _selectedLevels
    ){ logs, selectedTags, selectedLevels ->
        logs.filter { log ->
            (selectedTags.isEmpty() || log.tag in selectedTags) &&
            (selectedLevels.isEmpty() || log.level in selectedLevels)
        }
    }

    val tags = dao.getUniqueTags()
    val levels = dao.getUniqueLevels()

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
        viewModelScope.launch {
            dao.clear()
        }
    }
}