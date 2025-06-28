package com.oriooneee.axer

actual fun getStackTrace(throwable: Throwable): String {
    return throwable.stackTraceToString()
}