package com.oriooneee.ktorin.config

import com.oriooneee.ktorin.room.entities.Response

interface ResponseImportantSelector {
    suspend fun selectImportant(response: Response): List<String>
}