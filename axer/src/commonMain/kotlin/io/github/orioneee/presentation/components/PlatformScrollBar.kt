package io.github.orioneee.presentation.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable

@Composable
expect fun BoxScope.PlatformVerticalScrollBar(state: LazyListState)

@Composable
expect fun PlatformHorizontalScrollBar(scrollState: ScrollState)