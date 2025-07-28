package io.github.orioneee.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import io.github.orioneee.domain.other.DataState

@get:Composable
val LoadingContent: Unit
    get() {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Companion.Center

        ) {
            CircularProgressIndicator()
        }
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> ScreenLayout(
    state: DataState<T>,
    isEmpty: (T) -> Boolean,
    topAppBarTitle: String,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    emptyContent: @Composable BoxScope.() -> Unit,
    content: @Composable (T) -> Unit,
) {
    val isLoading = state is DataState.Loading<T>
    val isEmptyState = when (state) {
        is DataState.Success -> isEmpty(state.data) || state.data == null
        else -> false
    }

    ScreenLayout(
        isLoading = isLoading,
        isEmpty = isEmptyState,
        topAppBarTitle = topAppBarTitle,
        navigationIcon = navigationIcon,
        actions = actions,
        floatingActionButton = floatingActionButton,
        emptyContent = emptyContent,
        content = { if (state is DataState.Success) content(state.data) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenLayout(
    isLoading: Boolean,
    isEmpty: Boolean,
    topAppBarTitle: String,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    emptyContent: @Composable BoxScope.() -> Unit,
    content: @Composable () -> Unit,
) {
    Scaffold(
        floatingActionButton = {
            floatingActionButton()
        },
        topBar = {
            CenterAlignedTopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = {
                    Text(
                        topAppBarTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleSmall
                    )
                },
                actions = actions,
                navigationIcon = navigationIcon,
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> LoadingContent
                isEmpty -> emptyContent()
                else -> content()
            }
        }
    }
}