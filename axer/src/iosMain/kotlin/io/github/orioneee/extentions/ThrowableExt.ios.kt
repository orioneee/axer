package io.github.orioneee.extentions

import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalNativeApi::class)
actual fun Throwable.getPlatformStackTrace(): String {
    return message ?: "Unknown error"
}