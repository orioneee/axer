package io.github.orioneee.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun MultiplatformAlertDialog(
    canDismissByClickOutside: Boolean,
    isShowDialog: Boolean,
    onDismiss: () -> Unit,
    title: @Composable (() -> Unit),
    cancelButton: @Composable (() -> Unit)?,
    confirmButton: @Composable (() -> Unit),
    content: @Composable () -> Unit,
) {
    var visible by remember { mutableStateOf(isShowDialog) }
    val density = LocalDensity.current
    val maxOffsetDp = 30.dp

    val offsetFraction = remember { Animatable(1f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(isShowDialog) {
        if (isShowDialog) {
            visible = true
            launch { offsetFraction.animateTo(0f, animationSpec = tween(300)) }
            launch { alpha.animateTo(1f, animationSpec = tween(300)) }
        } else {
            val offsetAnimation =
                launch { offsetFraction.animateTo(-.5f, animationSpec = tween(300)) }
            val alphaAnimation = launch { alpha.animateTo(0f, animationSpec = tween(300)) }
            offsetAnimation.join()
            alphaAnimation.join()
            visible = false
            offsetFraction.snapTo(1f)
        }
    }

    if (visible) {
        AlertDialog(
            properties = DialogProperties(
                dismissOnClickOutside = canDismissByClickOutside
            ),
            modifier = Modifier
                .graphicsLayer {
                    translationY = with(density) {
                        offsetFraction.value * maxOffsetDp.toPx()
                    }
                    this.alpha = alpha.value
                },
            onDismissRequest = onDismiss,
            title = title,
            text = content,
            dismissButton = cancelButton,
            confirmButton = confirmButton,
        )
    }
}