package io.github.orioneee.presentation.components

import androidx.compose.material3.adaptive.Posture
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.toSize
import androidx.window.core.layout.WindowSizeClass

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun currentWindowAdaptiveInfo(): WindowAdaptiveInfo {
    val density = LocalDensity.current
    val windowInfo = LocalWindowInfo.current
    val size = with(density) { windowInfo.containerSize.toSize().toDpSize() }
    return WindowAdaptiveInfo(
        WindowSizeClass.compute(size.width.value, size.height.value),
        Posture() //postures and hinges are relevant to android devices only
    )
}
