package io.github.orioneee.internal.domain.other

sealed class DataState<T>{
    class Loading<T>() : DataState<T>()
    data class Success<T>(val data: T) : DataState<T>()
}