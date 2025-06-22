package com.oriooneee.ktorin.config

import com.oriooneee.ktorin.domain.Response

interface ResponseImportantSelector {
    suspend fun selectImportant(response: Response): List<String>
}