package io.github.orioneee.presentation.screens.exceptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.orioneee.room.dao.AxerExceptionDao
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

internal class ExceptionsViewModel(
    private val exceptionDao: AxerExceptionDao,
    exceptionID: Long? = null
) : ViewModel() {
    val exceptions = exceptionDao.getAll()

    val exceptionByID = exceptionDao.getByID(exceptionID).map {
        if (it == null) return@map null
        it
    }

    fun deleteAll() {
        viewModelScope.launch {
            exceptionDao.deleteAll()
        }
    }
}