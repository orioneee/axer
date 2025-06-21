package com.oriooneee.ktorin.presentation.viewmodels

import androidx.lifecycle.ViewModel
import com.oriooneee.ktorin.room.dao.RequestDao
import com.oriooneee.ktorin.room.entities.Transaction
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MainViewModel: ViewModel(), KoinComponent {
    private val dao: RequestDao by inject()

    suspend fun onSendRequest(request: Transaction) = dao.upsert(request)
}