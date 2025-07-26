package io.github.orioneee.presentation.screens.exceptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.orioneee.AxerDataProvider
import io.github.orioneee.extentions.successData
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

internal class ExceptionsViewModel(
    private val dataProvider: AxerDataProvider,
    exceptionID: Long? = null
) : ViewModel() {
    private var currentJob: Job? = null
    private val _isShowLoadingDialog = MutableStateFlow(false)

    val isShowLoadingDialog = _isShowLoadingDialog.asStateFlow().debounce(100)
    val exceptionsState = dataProvider.getAllExceptions()

    val exceptionByIDState = if (exceptionID != null) {
        dataProvider.getExceptionById(exceptionID)
    } else {
        flowOf()
    }

    fun deleteAll() {
        currentJob = viewModelScope.launch {
            try {
                _isShowLoadingDialog.value = true
                dataProvider.deleteAllExceptions()
            } finally {
                _isShowLoadingDialog.value = false
            }
        }
    }

    fun cancelCurrentJob() {
        currentJob?.cancel()
        currentJob = null
        _isShowLoadingDialog.value = false
    }
}