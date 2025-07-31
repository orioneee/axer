package io.github.orioneee.presentation.screens.database.tableList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.orioneee.Axer
import io.github.orioneee.AxerDataProvider
import io.github.orioneee.domain.database.DatabaseWrapped
import io.github.orioneee.extentions.successData
import io.github.orioneee.processors.RoomReader
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
internal class ListDatabaseViewModel(
    dataProvider: AxerDataProvider,
) : ViewModel() {

    val databases = dataProvider.getDatabases().shareIn(viewModelScope, SharingStarted.WhileSubscribed(), replay = 1)
}