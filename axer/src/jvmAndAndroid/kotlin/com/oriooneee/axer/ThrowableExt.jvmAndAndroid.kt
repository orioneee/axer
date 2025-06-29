package com.oriooneee.axer

actual fun Throwable.getPlatformStackTrace(): String = stackTraceToString()