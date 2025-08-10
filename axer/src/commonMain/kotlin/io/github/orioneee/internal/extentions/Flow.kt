package io.github.orioneee.internal.extentions

import io.github.orioneee.internal.domain.other.DataState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


internal fun <T> Flow<DataState<T>>.successData(): Flow<T?> {
    return map { state ->
        if (state is DataState.Success) {
            state.data
        } else {
            null
        }
    }
}