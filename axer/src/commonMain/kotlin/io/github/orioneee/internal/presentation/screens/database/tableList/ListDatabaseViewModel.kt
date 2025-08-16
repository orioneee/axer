package io.github.orioneee.internal.presentation.screens.database.tableList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.orioneee.internal.AxerDataProvider
import io.github.orioneee.internal.domain.other.DataState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.seconds

@OptIn(FlowPreview::class)
internal class ListDatabaseViewModel(
    dataProvider: AxerDataProvider,
) : ViewModel() {

    val databases = dataProvider.getDatabases()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(3.seconds),
            initialValue = DataState.Loading()
        )
}