package io.github.orioneee.internal.processors

import io.github.orioneee.internal.domain.exceptions.AxerException
import io.github.orioneee.internal.koin.IsolatedContext
import io.github.orioneee.internal.logger.getSavableError
import io.github.orioneee.internal.room.dao.AxerExceptionDao
import io.github.orioneee.internal.storage.AxerSettings
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

