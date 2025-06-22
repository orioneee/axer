package com.oriooneee.ktorin.presentation.screens

import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oriooneee.ktorin.domain.HighlightedBodyWrapper
import com.oriooneee.ktorin.room.dao.RequestDao
import com.oriooneee.ktorin.domain.Transaction
import dev.snipme.highlights.Highlights
import generateAnnotatedString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json


class RequestViewModel(
    private val requestDao: RequestDao,
    requestId: Long?
) : ViewModel() {

    suspend fun getHighlights(code: String): AnnotatedString {
        return Highlights
            .Builder(code = code)
            .build()
            .getHighlights()
            .generateAnnotatedString(code)
    }

    val json = Json {
        prettyPrint = true
    }
    val requests = requestDao.getAll().map {
        it.reversed()
    }
    private val _requestByID = requestDao.getById(requestId).map {
        if (it == null) return@map null
        val requestBody = it.requestBody
        val responseBody = it.responseBody
        val prittyRequestBody = try {
            requestBody?.let { json.encodeToString(json.parseToJsonElement(it)) }
        } catch (e: Exception) {
            requestBody
        }
        val prittyResponseBody = try {
            responseBody?.let { json.encodeToString(json.parseToJsonElement(it)) }
        } catch (e: Exception) {
            responseBody
        }

        val highlightedRequestBody = AnnotatedString(prittyRequestBody ?: "")
        val highlightedResponseBody = AnnotatedString(prittyResponseBody ?: "")

        viewModelScope.launch(Dispatchers.IO) {
            launch {
                if (prittyRequestBody != null) {
                    _highlightedRequestBody.value = getHighlights(prittyRequestBody)
                }
            }
            launch {
                if (prittyResponseBody != null) {
                    _highlightedResponseBody.value = getHighlights(prittyResponseBody)
                }
            }
        }

        HighlightedBodyWrapper(
            request = it.copy(
                requestBody = prittyRequestBody,
                responseBody = prittyResponseBody,
            ),
            highlightedRequestBody = highlightedRequestBody,
            highlightedResponseBody = highlightedResponseBody
        )
    }

    private val _highlightedRequestBody = MutableStateFlow<AnnotatedString>(AnnotatedString(""))
    private val _highlightedResponseBody = MutableStateFlow<AnnotatedString>(AnnotatedString(""))

    val requestByID = combine(
        _requestByID,
        _highlightedRequestBody,
        _highlightedResponseBody
    ) { request, highlightedRequestBody, highlightedResponseBody ->
        if (request == null) return@combine null
        HighlightedBodyWrapper(
            request = request.request,
            highlightedRequestBody = highlightedRequestBody.takeIf { it.isNotBlank() }
                ?: request.highlightedRequestBody,
            highlightedResponseBody = highlightedResponseBody.takeIf { it.isNotBlank() }
                ?: request.highlightedResponseBody
        )
    }

    val methodFilters = requests.map {
        it.map { it.method }
            .distinct()
            .sortedBy { it }
    }
    val imageFilters = requests.map {
        if(it.any { it.isImage == true }){
            listOf("Non Image", "Image")
        } else {
            listOf()
        }
    }

    private val _selectedMethods = MutableStateFlow<List<String>>(emptyList())
    private val _selectedImageFilter = MutableStateFlow<List<String>>(emptyList())

    val selectedMethods = _selectedMethods.asStateFlow()
    val selectedImageFilter = _selectedImageFilter.asStateFlow()

    val filteredRequests = combine(
        requests,
        _selectedMethods,
        _selectedImageFilter
    ) { requests, selectedMethods, selectedImageFilter ->
        val filterdByMethod = if (selectedMethods.isEmpty()) {
            requests
        } else {
            requests.filter { selectedMethods.contains(it.method) }
        }
        val filterdByImage = if (selectedImageFilter.isEmpty()) {
            filterdByMethod
        } else {
            filterdByMethod.filter {
                if (selectedImageFilter.contains("Image")) it.isImage == true
                else it.isImage == false
            }
        }
        filterdByImage
    }

    fun toggleMethodFilter(method: String) {
        viewModelScope.launch {
            _selectedMethods.value = if (_selectedMethods.value.contains(method)) {
                _selectedMethods.value - method
            } else {
                _selectedMethods.value + method
            }
        }
    }

    fun toggleImageFilter(filter: String) {
        viewModelScope.launch {
            _selectedImageFilter.value = if (_selectedImageFilter.value.contains(filter)) {
                _selectedImageFilter.value - filter
            } else {
                _selectedImageFilter.value + filter
            }
        }
    }

    fun clearMethodFilters() {
        viewModelScope.launch {
            _selectedMethods.value = emptyList()
        }
    }

    fun clearImageFilters() {
        viewModelScope.launch {
            _selectedImageFilter.value = emptyList()
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            requestDao.deleteAll()
        }
    }

    fun onViewed(transaction: Transaction) {
        viewModelScope.launch(Dispatchers.IO) {
            if(transaction.isViewed) return@launch
            requestDao.upsert(transaction.copy(isViewed = true))
        }
    }
}