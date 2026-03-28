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
import androidx.compose.runtime.CompositionLocalProvider
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
        primary = Color(0xFF4F46E5),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFE0E7FF),
        onPrimaryContainer = Color(0xFF312E81),
        inversePrimary = Color(0xFF818CF8),
        secondary = Color(0xFF0891B2),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFCFFAFE),
        onSecondaryContainer = Color(0xFF164E63),
        tertiary = Color(0xFF059669),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFD1FAE5),
        onTertiaryContainer = Color(0xFF064E3B),
        error = Color(0xFFDC2626),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFEE2E2),
        onErrorContainer = Color(0xFF7F1D1D),
        background = Color(0xFFF8FAFC),
        onBackground = Color(0xFF0F172A),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF1E293B),
        inverseSurface = Color(0xFF1E293B),
        inverseOnSurface = Color(0xFFF1F5F9),
        surfaceVariant = Color(0xFFF1F5F9),
        onSurfaceVariant = Color(0xFF64748B),
        outline = Color(0xFFCBD5E1),
    )
    val dark = darkColorScheme(
        primary = Color(0xFF818CF8),
        onPrimary = Color(0xFF1E1B4B),
        primaryContainer = Color(0xFF312E81),
        onPrimaryContainer = Color(0xFFC7D2FE),
        inversePrimary = Color(0xFF4F46E5),
        secondary = Color(0xFF22D3EE),
        onSecondary = Color(0xFF083344),
        secondaryContainer = Color(0xFF164E63),
        onSecondaryContainer = Color(0xFFCFFAFE),
        tertiary = Color(0xFF34D399),
        onTertiary = Color(0xFF022C22),
        tertiaryContainer = Color(0xFF064E3B),
        onTertiaryContainer = Color(0xFFA7F3D0),
        error = Color(0xFFF87171),
        onError = Color(0xFF450A0A),
        errorContainer = Color(0xFF7F1D1D),
        onErrorContainer = Color(0xFFFECACA),
        background = Color(0xFF0B0B11),
        onBackground = Color(0xFFE2E8F0),
        surface = Color(0xFF12121A),
        onSurface = Color(0xFFE2E8F0),
        inverseSurface = Color(0xFFE2E8F0),
        inverseOnSurface = Color(0xFF1E293B),
        surfaceVariant = Color(0xFF1A1A25),
        onSurfaceVariant = Color(0xFF94A3B8),
        outline = Color(0xFF334155),
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

    val isDark: Boolean
        @Composable get() {
            val currentTheme by AxerSettings.themeFlow.collectAsState(AxerSettings.theme.get())
            return when (currentTheme) {
                Theme.FOLLOW_SYSTEM -> isSystemInDarkTheme()
                Theme.LIGHT -> false
                Theme.DARK -> true
            }
        }
    @Composable
    fun ProvideTheme(
        content: @Composable () -> Unit
    ) {
        val axerColors = if (isDark) DarkAxerColors else LightAxerColors
        CompositionLocalProvider(LocalAxerColors provides axerColors) {
            MaterialTheme(currentColorScheme) {
                content()
            }
        }
    }
}