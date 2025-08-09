package io.github.orioneee.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentWithAxerFab(
    modifier: Modifier = Modifier,
    showFAB: Boolean = true,
    content: @Composable () -> Unit = {},
) {
    content()
}