package io.github.orioneee.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import io.github.orioneee.axer.debugger.generated.resources.Res
import io.github.orioneee.axer.debugger.generated.resources.jetbrains_mono_bold
import io.github.orioneee.axer.debugger.generated.resources.jetbrains_mono_regular
import io.github.orioneee.axer.debugger.generated.resources.jetbrains_mono_semibold
import org.jetbrains.compose.resources.Font

val JetbrainsMonoFontFamily: FontFamily
    @Composable
    get() = FontFamily(
        Font(Res.font.jetbrains_mono_regular, weight = FontWeight.Normal),
        Font(Res.font.jetbrains_mono_bold, weight = FontWeight.Bold),
        Font(Res.font.jetbrains_mono_semibold, weight = FontWeight.SemiBold)

    )