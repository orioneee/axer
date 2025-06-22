@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.oriooneee.axer.requestProcessor

import com.oriooneee.axer.domain.Transaction
import com.oriooneee.axer.koin.IsolatedContext
import com.oriooneee.axer.room.dao.RequestDao


class RequestProcessor() {
    private val dao: RequestDao by IsolatedContext.koin.inject()

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

expect suspend fun updateNotification(requests: List<Transaction>)