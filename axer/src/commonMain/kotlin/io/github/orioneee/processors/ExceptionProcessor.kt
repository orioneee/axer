package io.github.orioneee.processors

import io.github.orioneee.domain.exceptions.AxerException
import io.github.orioneee.koin.IsolatedContext
import io.github.orioneee.logger.getPlatformStackTrace
import io.github.orioneee.room.dao.AxerExceptionDao
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
        val exception = AxerException(
            message = exception.message ?: "Unknown message",
            stackTrace = exception.getPlatformStackTrace(),
            time = Clock.System.now().toEpochMilliseconds(),
            isFatal = isFatal,
            shortName = simpleName,
        )
        dao.upsert(exception)
        notifyAboutException(exception)
    }
}

internal expect fun notifyAboutException(exception: AxerException)

