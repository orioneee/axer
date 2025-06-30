@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.orioneee.requestProcessor

import io.github.orioneee.domain.requests.Transaction
import io.github.orioneee.koin.IsolatedContext
import io.github.orioneee.room.dao.RequestDao


internal class RequestProcessor() {
    private val dao: RequestDao by IsolatedContext.koin.inject()

    suspend fun deleteRequestIfNotFiltred(id: Long){
        dao.deleteById(id)
    }

    suspend fun onSend(request: Transaction): Long {
        val id = dao.upsert(request)
        val firstFive = dao.getFirstFive()
        updateNotification(firstFive)
        return id
    }

    suspend fun onFailed(request: Transaction) {
        dao.upsert(request)
        val firstFive = dao.getFirstFive()
        updateNotification(firstFive)
    }

    suspend fun onFinished(request: Transaction) {
        dao.upsert(request)
        val firstFive = dao.getFirstFive()
        updateNotification(firstFive)
    }
}

internal expect suspend fun updateNotification(requests: List<Transaction>)