package com.oriooneee.ktorin.domain

import androidx.compose.ui.text.AnnotatedString

data class HighlightedBodyWrapper(
    val request: Transaction,
    val highlightedRequestBody: AnnotatedString,
    val highlightedResponseBody: AnnotatedString
)