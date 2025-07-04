package io.github.orioneee.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue

internal expect object LocalAppLocale {
    val current: String

    @Composable
    infix fun provides(value: String?): ProvidedValue<*>
}
