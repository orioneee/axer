@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.orioneee.internal.processors

import io.github.orioneee.internal.domain.requests.data.Transaction
import io.github.orioneee.internal.domain.requests.data.TransactionFull
import io.github.orioneee.internal.extentions.byteSize
import io.github.orioneee.internal.koin.IsolatedContext
import io.github.orioneee.internal.room.dao.RequestDao
import io.github.orioneee.internal.storage.AxerSettings


internal class RequestProcessor(
    private val requestMaxAge: Long,
    private val maxTotalRequestSize: Long,
) {
    private val dao: RequestDao by IsolatedContext.koin.inject()

    suspend fun deleteRequestIfNotFiltred(id: Long) {
        dao.deleteById(id)
    }

    suspend fun onSend(request: TransactionFull): Long {
        dao.deleteAllWhichOlderThan(requestMaxAge)
        dao.trimToMaxSize(maxTotalRequestSize)
        val id = dao.upsert(request)
        val firstFive = dao.getFirstFiveNotReaded()
        if (AxerSettings.isSendNotification.get()) {
            updateNotification(firstFive)
        }
        return id
    }

    suspend fun onFailed(request: TransactionFull) {
        val requestWithSize = request.copy(size = request.byteSize())
        dao.upsert(requestWithSize)
        val firstFive = dao.getFirstFiveNotReaded()
        if (AxerSettings.isSendNotification.get()) {
            updateNotification(firstFive)
        }
    }

    suspend fun onFinished(request: TransactionFull) {
        val requestWithSize = request.copy(size = request.byteSize())
        dao.upsert(requestWithSize)
        val firstFive = dao.getFirstFiveNotReaded()
        if (AxerSettings.isSendNotification.get()) {
            updateNotification(firstFive)
        }
    }
}

internal expect suspend fun updateNotification(requests: List<Transaction>)