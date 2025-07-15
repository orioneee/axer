package io.github.orioneee.presentation.screens.exceptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.orioneee.provider.AxerDataProvider
import io.github.orioneee.room.dao.AxerExceptionDao
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

internal class ExceptionsViewModel(
    private val dataProvider: AxerDataProvider,
    exceptionID: Long? = null
) : ViewModel() {
    val exceptions = dataProvider.getAllExceptions()

    val exceptionByID = if (exceptionID != null) {
        dataProvider.getExceptionById(exceptionID).map {
            if (it == null) return@map null
            it
        }
    } else {
        flowOf()
    }

    fun deleteAll() {
        viewModelScope.launch {
            dataProvider.deleteAllExceptions()
        }
    }
}