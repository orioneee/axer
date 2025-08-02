package io.github.orioneee.processors

import io.github.orioneee.domain.exceptions.AxerException
import io.github.orioneee.koin.IsolatedContext
import io.github.orioneee.logger.getSavableError
import io.github.orioneee.room.dao.AxerExceptionDao
import io.github.orioneee.storage.AxerSettings
import kotlinx.coroutines.runBlocking
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

internal class ExceptionProcessor() {
    private val dao: AxerExceptionDao by IsolatedContext.koin.inject()

    @OptIn(ExperimentalTime::class)
    fun onException(
        exception: Throwable,
        isFatal: Boolean,
        onRecorded: () -> Unit = {}
    ) = runBlocking {
        val exception = AxerException(
            time = Clock.System.now().toEpochMilliseconds(),
            isFatal = isFatal,
            error = exception.getSavableError(),
            sessionIdentifier = SessionManager.sessionId
        )
        dao.upsert(exception)
        if (AxerSettings.isSendNotification.get()) {
            notifyAboutException(exception)
        }
        onRecorded()
    }
}

internal expect fun notifyAboutException(exception: AxerException)

