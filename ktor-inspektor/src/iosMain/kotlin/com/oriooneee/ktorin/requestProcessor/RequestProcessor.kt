package com.oriooneee.ktorin.requestProcessor

import com.oriooneee.ktorin.room.dao.RequestDao
import com.oriooneee.ktorin.room.entities.Transaction
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

actual class RequestProcessor {
    private val dao: RequestDao = IsolatedContext.koin.inject()
    actual suspend fun onSend(request: Transaction) = dao.upsert(request)
    actual suspend fun onFailed(request: Transaction){
        dao.upsert(request)
    }
    actual suspend fun onFinished(request: Transaction){
        dao.upsert(request)
    }
}