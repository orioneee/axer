package io.github.orioneee.presentation.screens.database.allQueries

import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.orioneee.processors.RoomReader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class AllQueriesViewModel(
): ViewModel() {
    private val reader = RoomReader()
    val allQueryFlow = MutableStateFlow<List<String>>(listOf())

    init {
        reader.axerDriver.allQueryFlow
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
