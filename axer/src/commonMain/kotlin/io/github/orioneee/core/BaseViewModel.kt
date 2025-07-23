package io.github.orioneee.core

import androidx.lifecycle.ViewModel
import io.github.orioneee.domain.other.DataState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

open class BaseViewModel : ViewModel() {

    fun showMessage(message: String) {
        println("Message: $message")
    }

    fun <T> Flow<DataState<List<T>>>.successData(): Flow<List<T>> {
        return map { state ->
            if (state is DataState.Success) {
                state.data
            } else {
                emptyList()
            }
        }
    }

    fun <T> Flow<DataState<T>>.successData(): Flow<T?> {
        return map { state ->
            if (state is DataState.Success) {
                state.data
            } else {
                null
            }
        }
    }
}