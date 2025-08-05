package io.github.orioneee.presentation.screens.database.allQueries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.orioneee.AxerDataProvider
import io.github.orioneee.extentions.successData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class AllQueriesViewModel(
    provider: AxerDataProvider
) : ViewModel() {
    val allQueryFlow = MutableStateFlow<List<String>>(listOf())


    init {
        provider.getAllQueries()
            .successData()
            .filterNotNull()
            .onEach {
                val currentQueries = allQueryFlow.value
                val newQueries = listOf(it) + currentQueries
                allQueryFlow.value = newQueries
            }
            .launchIn(viewModelScope)
    }

    fun clearQueries() {
        allQueryFlow.value = listOf()
    }
}
