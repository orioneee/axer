package com.oriooneee.ktorin.config

import com.oriooneee.ktorin.domain.Request

interface RequestImportantSelector{
    suspend fun selectImportant(request: Request): List<String>
}