package io.github.orioneee.presentation.components

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

internal fun buildStringSection(
    title: String,
    content: String,
    separator: String = ": "
) = buildAnnotatedString {
    withStyle(
        style = SpanStyle(
            fontWeight = FontWeight.Bold
        )
    ) {
        append(title + separator)
    }
    append(content)
}