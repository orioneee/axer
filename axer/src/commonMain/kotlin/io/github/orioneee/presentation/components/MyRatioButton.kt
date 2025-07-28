package io.github.orioneee.presentation.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.orioneee.extentions.clickableWithoutRipple


@Composable
fun PhantomMyRatioButton(
    modifier: Modifier = Modifier,
    selected: Boolean,
) {
    val dotRadius =
        animateDpAsState(
            targetValue = if (selected) 4.dp else 0.dp,
            animationSpec = tween(durationMillis = 100)
        )
    val radioColor = MaterialTheme.colorScheme.surface
    Canvas(
        modifier
            .size(24.dp)
            .padding(2.dp)

    ) {
        val strokeWidth = 2.dp.toPx()
        drawCircle(
            color = radioColor,
            radius = (9.dp).toPx() - strokeWidth / 2,
            style = Fill
        )
    }
}

@Composable
fun MyRatioButton(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val dotRadius =
        animateDpAsState(
            targetValue = if (selected) 4.dp else 0.dp,
            animationSpec = tween(durationMillis = 100)
        )
    val radioColor = MaterialTheme.colorScheme.primary
    Canvas(
        modifier
            .clickableWithoutRipple {
                onClick?.invoke()
            }
            .size(24.dp)
            .padding(2.dp)

    ) {
        val strokeWidth = 2.dp.toPx()
        drawCircle(
            radioColor,
            radius = (8.dp).toPx() - strokeWidth / 2,
            style = Stroke(strokeWidth)
        )
        if (dotRadius.value > 0.dp) {
            drawCircle(
                radioColor,
                dotRadius.value.toPx() - strokeWidth / 2,
                style = Fill
            )
        }
    }
}

@Composable
fun MyVerticalLine(
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    stroke: Dp = 2.dp,
    hitWidth: Dp = 24.dp
) {
    Canvas(
        modifier
            .clickableWithoutRipple { onClick?.invoke() }
            .then(modifier)
            .width(hitWidth)
    ) {
        val centerX = size.width / 2
        drawLine(
            color = color,
            start = Offset(centerX, 0f),
            end = Offset(centerX, size.height),
            strokeWidth = stroke.toPx(),
            cap = StrokeCap.Round
        )
    }
}
@Composable
fun MyVerticalLine(
    onClick: (() -> Unit)?,
    isFirst: Boolean,
    isLast: Boolean,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    stroke: Dp = 2.dp,
    hitWidth: Dp = 24.dp
) {
    Canvas(
        modifier
            .clickableWithoutRipple { onClick?.invoke() }
            .then(modifier)
            .width(hitWidth)
    ) {
        val centerX = size.width / 2
        val startY = if (isFirst) size.height / 2 else 0f
        val endY = if (isLast) size.height / 2 else size.height

        drawLine(
            color = color,
            start = Offset(centerX, startY),
            end = Offset(centerX, endY),
            strokeWidth = stroke.toPx(),
            cap = StrokeCap.Round
        )
    }
}


