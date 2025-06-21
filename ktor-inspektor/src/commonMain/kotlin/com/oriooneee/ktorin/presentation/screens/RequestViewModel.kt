package com.oriooneee.ktorin.presentation.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oriooneee.ktorin.room.dao.RequestDao
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RequestViewModel(
    private val requestDao: RequestDao,
    requestId: Long?
) : ViewModel() {
    val json = Json {
        prettyPrint = true
    }
    val requests = requestDao.getAll().map {
        it.reversed()
    }
    val requestByID = requestDao.getById(requestId).map {
        if(it == null) return@map null
        val requestBody = it.requestBody
        val responseBody = it.responseBody
        val prittyRequestBody = try{
            requestBody?.let { json.encodeToString(json.parseToJsonElement(it)) }
        } catch (e: Exception) {
            requestBody
        }
        val prittyResponseBody = try {
            responseBody?.let { json.encodeToString(json.parseToJsonElement(it)) }
        } catch (e: Exception) {
            responseBody
        }
        it.copy(
            requestBody = prittyRequestBody,
            responseBody = prittyResponseBody,
        )
    }

    fun clearAll() {
        viewModelScope.launch {
            requestDao.deleteAll()
        }
    }
}