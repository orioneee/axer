package com.oriooneee.axer.presentation.screens.sandbox

import androidx.lifecycle.ViewModel
import com.oriooneee.axer.room.dao.RequestDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class SandboxViewModel(
    private val requestDao: RequestDao,
    requestId: Long?
) : ViewModel() {
    val json = Json {
        prettyPrint = true
    }
    private val _selectedUrl: MutableStateFlow<String> = MutableStateFlow("")
    private val _selectedHeaders: MutableStateFlow<Map<String, String>> = MutableStateFlow(emptyMap())

    val selectedUrl = _selectedUrl
    val selectedHeaders = _selectedHeaders.map {
        it + mapOf("" to "")
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
        ).also {
            _selectedUrl.value = it.host + it.path
            _selectedHeaders.value = it.requestHeaders
        }
    }

    fun setUrl(url: String) {
        _selectedUrl.value = url
    }

    fun setHeader(
        index: Int,
        key: String,
        value: String
    ){
        val currentHeaders = _selectedHeaders.value.toList()
        val before = currentHeaders.subList(0, index)
        val after = currentHeaders.subList(index + 1, currentHeaders.size)
        val newHeaders = before + Pair(key, value) + after
        _selectedHeaders.value = newHeaders.toMap()

    }
}