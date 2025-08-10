package io.github.orioneee.internal.presentation.screens.exceptions.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.orioneee.internal.AxerDataProvider
import io.github.orioneee.internal.domain.exceptions.SessionException
import io.github.orioneee.internal.snackbarProcessor.SnackBarController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class ExceptionDetailsViewModel(
    private val dataProvider: AxerDataProvider,
    val exceptionID: Long?
) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    private val _data = MutableStateFlow<SessionException?>(null)

    val isLoading = _isLoading.asStateFlow()
    val data = _data.asStateFlow()

    private fun fetchData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val events = dataProvider.getSessionEventsByException(exceptionID!!)
                events.onFailure {
                    SnackBarController.showSnackBar(text = "Failed to load exception details: ${it.message}")
                }
                _data.value = events.getOrNull()
            } catch (e: Exception) {
            } finally {
                _isLoading.value = false
            }
        }
    }

    init {
        fetchData()
    }
}