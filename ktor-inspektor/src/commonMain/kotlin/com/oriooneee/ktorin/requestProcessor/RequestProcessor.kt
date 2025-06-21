package com.oriooneee.ktorin.requestProcessor

import com.oriooneee.ktorin.room.entities.Transaction

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class RequestProcessor (){
    suspend fun onSend(request: Transaction): Long
    suspend fun onFailed(request: Transaction)
    suspend fun onFinished(request: Transaction)
}