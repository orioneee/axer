package com.oriooneee.axer

import com.oriooneee.axer.domain.exceptions.AxerException
import com.oriooneee.axer.koin.IsolatedContext
import com.oriooneee.axer.room.dao.AxerExceptionDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

internal class ExceptionProcessor() {
    private val dao: AxerExceptionDao by IsolatedContext.koin.inject()

    @OptIn(ExperimentalTime::class)
    fun onException(
        exception: Throwable,
        isFatal: Boolean,
    ) = CoroutineScope(Dispatchers.IO).launch {
        val exception = AxerException(
            message = exception.message ?: "Unknown error",
            stackTrace = exception.stackTraceToString().take(256),
            time = Clock.System.now().toEpochMilliseconds(),
            isFatal = isFatal,
            shortName = exception::class.simpleName ?: "UnknownException",
        )
        dao.upsert(exception)
        notifyAboutException(exception)
    }
}

internal expect fun notifyAboutException(exception: AxerException)

