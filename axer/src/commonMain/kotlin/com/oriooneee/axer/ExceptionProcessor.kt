package com.oriooneee.axer

import com.oriooneee.axer.domain.exceptions.AxerException
import com.oriooneee.axer.koin.IsolatedContext
import com.oriooneee.axer.room.dao.AxerExceptionDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

internal class ExceptionProcessor() {
    private val dao: AxerExceptionDao by IsolatedContext.koin.inject()

    @OptIn(ExperimentalTime::class)
    fun onException(
        exception: Throwable,
        simpleName: String,
        isFatal: Boolean,
    ) = runBlocking {
        println("Recording exception: ${exception.message} as $simpleName, isFatal: $isFatal")
        val exception = AxerException(
            message = exception.message ?: "Unknown message",
            stackTrace = getStackTrace(exception),
            time = Clock.System.now().toEpochMilliseconds(),
            isFatal = isFatal,
            shortName = simpleName,
        )
        println("Exception object created: $exception")
        dao.upsert(exception)
        println("Exception recorded: $exception")
        notifyAboutException(exception)
    }
}

internal expect fun notifyAboutException(exception: AxerException)
internal expect fun getStackTrace(throwable: Throwable): String

