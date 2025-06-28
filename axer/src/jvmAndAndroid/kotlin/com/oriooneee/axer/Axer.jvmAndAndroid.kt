package com.oriooneee.axer

actual fun installErrorHandler() {
    Thread.setDefaultUncaughtExceptionHandler(AxerUncaughtExceptionHandler())
}