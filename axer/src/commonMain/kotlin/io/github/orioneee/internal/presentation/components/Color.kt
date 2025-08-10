package io.github.orioneee.internal.presentation.components

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

internal val Color.Companion.Warning: Color
    get() = Color(255,153,102)

internal val ColorScheme.warning: Color
    get() = Color.Warning