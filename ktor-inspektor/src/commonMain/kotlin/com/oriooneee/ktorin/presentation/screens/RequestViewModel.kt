package com.oriooneee.ktorin.presentation.screens

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oriooneee.ktorin.room.dao.RequestDao
import com.oriooneee.ktorin.room.entities.Request
import com.oriooneee.ktorin.room.entities.Transaction
import dev.snipme.highlights.Highlights
import generateAnnotatedString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class HighlightedBodyWrapper(
    val request: Transaction,
    val highlightedRequestBody: AnnotatedString,
    val highlightedResponseBody: AnnotatedString
)

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
            highlightedRequestBody = highlightedRequestBody.takeIf { it.isNotBlank() } ?: request.highlightedRequestBody,
            highlightedResponseBody = highlightedResponseBody.takeIf { it.isNotBlank() } ?: request.highlightedResponseBody
        )
    }

    fun clearAll() {
        viewModelScope.launch {
            requestDao.deleteAll()
        }
    }
}