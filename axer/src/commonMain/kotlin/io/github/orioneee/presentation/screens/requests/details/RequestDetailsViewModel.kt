package io.github.orioneee.presentation.screens.requests.details

import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.orioneee.AxerDataProvider
import io.github.orioneee.core.BaseViewModel
import io.github.orioneee.domain.requests.data.Transaction
import io.github.orioneee.domain.requests.formatters.BodyType
import io.github.orioneee.domain.requests.formatters.formatCSS
import io.github.orioneee.domain.requests.formatters.formatJavascript
import io.github.orioneee.extentions.formatJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import io.github.orioneee.domain.requests.formatters.formatXml
import io.github.orioneee.extentions.successData
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged

internal class RequestDetailsViewModel(
    private val dataProvider: AxerDataProvider,
    requestId: Long
) : BaseViewModel() {
    val json = Json {
        prettyPrint = true
    }

    private val _selectedRequestBodyFormat = MutableStateFlow<BodyType?>(null)
    private val _selectedResponseBodyFormat = MutableStateFlow<BodyType?>(null)

    val selectedRequestBodyFormat = _selectedRequestBodyFormat.asStateFlow()
    val selectedResponseBodyFormat = _selectedResponseBodyFormat.asStateFlow()

    val requestByIDState = dataProvider.getRequestById(requestId).distinctUntilChanged()
    val requestByID = requestByIDState.successData()

    fun onRequestBodyFormatSelected(bodyType: BodyType) {
        _selectedRequestBodyFormat.value = bodyType
    }
    fun onResponseBodyFormatSelected(bodyType: BodyType) {
        _selectedResponseBodyFormat.value = bodyType
    }

    fun onViewed(transaction: Transaction) {
        viewModelScope.launch(Dispatchers.IO) {
            if (transaction.isViewed) return@launch
            dataProvider.markAsViewed(transaction.id)
        }
    }
}