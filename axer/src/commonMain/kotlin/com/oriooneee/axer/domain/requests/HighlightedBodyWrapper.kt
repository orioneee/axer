package com.oriooneee.axer.domain.requests

import androidx.compose.ui.text.AnnotatedString

internal data class HighlightedBodyWrapper(
    val request: Transaction,
    val highlightedRequestBody: AnnotatedString,
    val highlightedResponseBody: AnnotatedString
)