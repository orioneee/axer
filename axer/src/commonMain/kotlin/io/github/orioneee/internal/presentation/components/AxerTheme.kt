package io.github.orioneee.internal.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import io.github.orioneee.internal.domain.other.Theme
import io.github.orioneee.internal.storage.AxerSettings

/**
 * @suppress
 */
object AxerTheme {
    val light = lightColorScheme(
        primary = Color(0xFF435E91),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFD8E2FF),
        onPrimaryContainer = Color(0xFF001A41),
        inversePrimary = Color(0xFFADC7FF),
        secondary = Color(0xFF565E71),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFDBE2F9),
        onSecondaryContainer = Color(0xFF131B2C),
        tertiary = Color(0xFF715574),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFFBD7FC),
        onTertiaryContainer = Color(0xFF29132D),
        error = Color(0xFFB3261E),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFF9DEDC),
        onErrorContainer = Color(0xFF410E0B),
        background = Color(0xFFFEFBFF),
        onBackground = Color(0xFF1B1B1F),
        surface = Color(0xFFFEFBFF),
        onSurface = Color(0xFF1B1B1F),
        inverseSurface = Color(0xFF303033),
        inverseOnSurface = Color(0xFFF2F0F4),
        surfaceVariant = Color(0xFFE1E2EC),
        onSurfaceVariant = Color(0xFF44474F),
        outline = Color(0xFF73767E),
    )
    val dark = darkColorScheme(
        primary = Color(0xFFADC7FF),
        onPrimary = Color(0xFF0F2F60),
        primaryContainer = Color(0xFF2A4678),
        onPrimaryContainer = Color(0xFFD8E2FF),
        inversePrimary = Color(0xFF435E91),
        secondary = Color(0xFFBFC6DC),
        onSecondary = Color(0xFF283041),
        secondaryContainer = Color(0xFF3F4759),
        onSecondaryContainer = Color(0xFFDBE2F9),
        tertiary = Color(0xFFDEBCDF),
        onTertiary = Color(0xFF402843),
        tertiaryContainer = Color(0xFF583E5B),
        onTertiaryContainer = Color(0xFFFBD7FC),
        error = Color(0xFFF2B8B5),
        onError = Color(0xFF601410),
        errorContainer = Color(0xFF8C1D18),
        onErrorContainer = Color(0xFFF9DEDC),
        background = Color(0xFF1B1B1F),
        onBackground = Color(0xFFE3E2E6),
        surface = Color(0xFF1B1B1F),
        onSurface = Color(0xFFE3E2E6),
        inverseSurface = Color(0xFFE3E2E6),
        inverseOnSurface = Color(0xFF303033),
        surfaceVariant = Color(0xFF44474F),
        onSurfaceVariant = Color(0xFFC4C6D0),
        outline = Color(0xFF8E9099),
    )

    @Composable
    fun getAnimatedColorScheme(
        scheme: ColorScheme
    ): ColorScheme {
        val spec: AnimationSpec<Color> = spring(stiffness = Spring.StiffnessMediumLow)
        return ColorScheme(
            primary = animateColorAsState(scheme.primary, animationSpec = spec).value,
            onPrimary = animateColorAsState(scheme.onPrimary, animationSpec = spec).value,
            primaryContainer = animateColorAsState(
                scheme.primaryContainer,
                animationSpec = spec
            ).value,
            onPrimaryContainer = animateColorAsState(
                scheme.onPrimaryContainer,
                animationSpec = spec
            ).value,
            inversePrimary = animateColorAsState(scheme.inversePrimary, animationSpec = spec).value,
            secondary = animateColorAsState(scheme.secondary, animationSpec = spec).value,
            onSecondary = animateColorAsState(scheme.onSecondary, animationSpec = spec).value,
            secondaryContainer = animateColorAsState(
                scheme.secondaryContainer,
                animationSpec = spec
            ).value,
            onSecondaryContainer = animateColorAsState(
                scheme.onSecondaryContainer,
                animationSpec = spec
            ).value,
            tertiary = animateColorAsState(scheme.tertiary, animationSpec = spec).value,
            onTertiary = animateColorAsState(scheme.onTertiary, animationSpec = spec).value,
            tertiaryContainer = animateColorAsState(
                scheme.tertiaryContainer,
                animationSpec = spec
            ).value,
            onTertiaryContainer = animateColorAsState(
                scheme.onTertiaryContainer,
                animationSpec = spec
            ).value,
            background = animateColorAsState(scheme.background, animationSpec = spec).value,
            onBackground = animateColorAsState(scheme.onBackground, animationSpec = spec).value,
            surface = animateColorAsState(scheme.surface, animationSpec = spec).value,
            onSurface = animateColorAsState(scheme.onSurface, animationSpec = spec).value,
            surfaceVariant = animateColorAsState(scheme.surfaceVariant, animationSpec = spec).value,
            onSurfaceVariant = animateColorAsState(
                scheme.onSurfaceVariant,
                animationSpec = spec
            ).value,
            surfaceTint = animateColorAsState(scheme.surfaceTint, animationSpec = spec).value,
            inverseSurface = animateColorAsState(scheme.inverseSurface, animationSpec = spec).value,
            inverseOnSurface = animateColorAsState(
                scheme.inverseOnSurface,
                animationSpec = spec
            ).value,
            error = animateColorAsState(scheme.error, animationSpec = spec).value,
            onError = animateColorAsState(scheme.onError, animationSpec = spec).value,
            errorContainer = animateColorAsState(scheme.errorContainer, animationSpec = spec).value,
            onErrorContainer = animateColorAsState(
                scheme.onErrorContainer,
                animationSpec = spec
            ).value,
            outline = animateColorAsState(scheme.outline, animationSpec = spec).value,
            outlineVariant = animateColorAsState(scheme.outlineVariant, animationSpec = spec).value,
            scrim = animateColorAsState(scheme.scrim, animationSpec = spec).value,
            surfaceBright = animateColorAsState(scheme.surfaceBright, animationSpec = spec).value,
            surfaceDim = animateColorAsState(scheme.surfaceDim, animationSpec = spec).value,
            surfaceContainer = animateColorAsState(
                scheme.surfaceContainer,
                animationSpec = spec
            ).value,
            surfaceContainerHigh = animateColorAsState(
                scheme.surfaceContainerHigh,
                animationSpec = spec
            ).value,
            surfaceContainerHighest = animateColorAsState(
                scheme.surfaceContainerHighest,
                animationSpec = spec
            ).value,
            surfaceContainerLow = animateColorAsState(
                scheme.surfaceContainerLow,
                animationSpec = spec
            ).value,
            surfaceContainerLowest = animateColorAsState(
                scheme.surfaceContainerLowest,
                animationSpec = spec
            ).value,
        )
    }

    val currentColorScheme: ColorScheme
        @Composable get() {
            val currentTheme by AxerSettings.themeFlow.collectAsState(AxerSettings.theme.get())
            val isDark = when (currentTheme) {
                Theme.FOLLOW_SYSTEM -> isSystemInDarkTheme()
                Theme.LIGHT -> false
                Theme.DARK -> true
            }
            return getAnimatedColorScheme(if (isDark) dark else light)
        }

    val systemColorScheme: ColorScheme
        @Composable get() = if (isSystemInDarkTheme()) dark else light


    @Composable
    fun ProvideTheme(
        content: @Composable () -> Unit
    ) {
        MaterialTheme(currentColorScheme) {
            content()
        }
    }
}