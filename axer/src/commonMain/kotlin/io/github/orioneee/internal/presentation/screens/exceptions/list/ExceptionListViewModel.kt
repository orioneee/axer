package io.github.orioneee.internal.presentation.screens.exceptions.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.orioneee.internal.AxerDataProvider
import io.github.orioneee.internal.snackbarProcessor.SnackBarController
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

internal class ExceptionListViewModel(
    private val dataProvider: AxerDataProvider,
) : ViewModel() {
    private var currentJob: Job? = null
    private val _isShowLoadingDialog = MutableStateFlow(false)

    @OptIn(FlowPreview::class)
    val isShowLoadingDialog = _isShowLoadingDialog.asStateFlow().debounce(100)
    val exceptionsState = dataProvider.getAllExceptions()

    fun deleteAll() {
        currentJob = viewModelScope.launch {
            try {
                _isShowLoadingDialog.value = true
                val res = dataProvider.deleteAllExceptions()
                res.onFailure {
                    SnackBarController.showSnackBar(
                        "Failed to delete all exceptions: ${it.message ?: "Unknown error"}"
                    )
                }
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