package io.github.orioneee.internal.presentation.screens.database.allQueries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.orioneee.internal.AxerDataProvider
import io.github.orioneee.internal.extentions.successData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

internal class AllQueriesViewModel(
    provider: AxerDataProvider
) : ViewModel() {
    val allQueryFlow = MutableStateFlow<List<String>>(listOf())


    init {
        provider.getAllQueries()
            .successData()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = null
            )
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
