package com.oriooneee.axer.presentation.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

internal object Animations {
    const val ANIMATION_DURATION = 300
    private const val OFFSET_MULTIPLIER = 0.3f
    
    val exitTransition = slideOutHorizontally(
        targetOffsetX = { fullWidth -> (-fullWidth * OFFSET_MULTIPLIER).toInt() },
        animationSpec = tween(durationMillis = ANIMATION_DURATION, easing = FastOutSlowInEasing)
    ) + fadeOut(
        animationSpec = tween(durationMillis = ANIMATION_DURATION, easing = FastOutSlowInEasing),
        targetAlpha = 0.05f
    )
    val popEnterTransition = slideInHorizontally(
        initialOffsetX = { fullWidth -> (-fullWidth * OFFSET_MULTIPLIER).toInt() },
        animationSpec = tween(durationMillis = ANIMATION_DURATION, easing = FastOutSlowInEasing)
    ) + fadeIn(
        initialAlpha = OFFSET_MULTIPLIER,
        animationSpec = tween(durationMillis = ANIMATION_DURATION, easing = FastOutSlowInEasing)
    )
    val enterTransition = slideInHorizontally(
        initialOffsetX = { fullWidth -> fullWidth },
        animationSpec = tween(
            durationMillis = ANIMATION_DURATION,
            easing = FastOutSlowInEasing
        )
    )
    val popExitTransition = slideOutHorizontally(
        targetOffsetX = { fullWidth -> fullWidth },
        animationSpec = tween(durationMillis = ANIMATION_DURATION, easing = FastOutSlowInEasing)
    )
}