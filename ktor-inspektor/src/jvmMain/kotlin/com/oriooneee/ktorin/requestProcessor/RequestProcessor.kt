@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.oriooneee.ktorin.requestProcessor

import com.oriooneee.ktorin.koin.IsolatedContext
import com.oriooneee.ktorin.room.dao.RequestDao
import com.oriooneee.ktorin.domain.Transaction
import org.koin.core.component.KoinComponent

actual class RequestProcessor : KoinComponent {
    private val dao: RequestDao by IsolatedContext.koin.inject()
    actual suspend fun onSend(request: Transaction) = dao.upsert(request)
    actual suspend fun onFailed(request: Transaction){
        dao.upsert(request)
    }
    actual suspend fun onFinished(request: Transaction){
        dao.upsert(request)
    }
}