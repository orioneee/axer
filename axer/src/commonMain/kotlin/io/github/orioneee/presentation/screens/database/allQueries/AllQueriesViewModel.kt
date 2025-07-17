package io.github.orioneee.presentation.screens.database.allQueries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.orioneee.AxerDataProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class AllQueriesViewModel(
    provider: AxerDataProvider
) : ViewModel() {
    val allQueryFlow = MutableStateFlow<List<String>>(listOf())


    init {
        provider.getAllQueries()
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
