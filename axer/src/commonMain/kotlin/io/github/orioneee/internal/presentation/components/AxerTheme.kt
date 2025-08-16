package io.github.orioneee.internal.presentation.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    fun ProvideTheme(
        content: @Composable () -> Unit
    ) {
        val currentTheme by AxerSettings.theme.asFlow()
            .collectAsStateWithLifecycle(Theme.FOLLOW_SYSTEM)
        val isDark = when (currentTheme) {
            Theme.FOLLOW_SYSTEM -> isSystemInDarkTheme()
            Theme.LIGHT -> false
            Theme.DARK -> true
        }
        val scheme = if (isDark) dark else light
        MaterialTheme(scheme) {
            content()
        }
    }
}