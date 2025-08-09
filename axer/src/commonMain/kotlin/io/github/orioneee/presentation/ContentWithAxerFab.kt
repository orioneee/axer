package io.github.orioneee.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.safeGesturesPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import io.github.orioneee.Axer

expect val isAvailableFab: Boolean

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentWithAxerFab(
    modifier: Modifier = Modifier,
    showFAB: Boolean = isAvailableFab,
    content: @Composable () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .zIndex(Float.MAX_VALUE)
            .fillMaxSize()
            .safeGesturesPadding()
            .safeContentPadding()
            .then(modifier)
    ) {
        if (showFAB) {
            AxerFab(onClick = Axer::openAxerUI)
        }
    }
    content()
}