package io.github.orioneee.internal.presentation.screens.database.tableList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.orioneee.internal.AxerDataProvider
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.shareIn
import kotlin.time.Duration.Companion.seconds

@OptIn(FlowPreview::class)
internal class ListDatabaseViewModel(
    dataProvider: AxerDataProvider,
) : ViewModel() {

    val databases = dataProvider.getDatabases()
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(3.seconds), replay = 1)
}