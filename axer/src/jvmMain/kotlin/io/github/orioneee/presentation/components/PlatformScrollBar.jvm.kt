package io.github.orioneee.presentation.components

import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
actual fun BoxScope.PlatformVerticalScrollBar(state: LazyListState) {
    VerticalScrollbar(
        modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
        adapter = rememberScrollbarAdapter(
            scrollState = state
        )
    )
}

@Composable
actual fun PlatformHorizontalScrollBar(scrollState: ScrollState) {
    HorizontalScrollbar(
        modifier = Modifier
            .fillMaxWidth(),
        adapter = rememberScrollbarAdapter(
            scrollState = scrollState
        )
    )
}