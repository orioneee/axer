package com.oriooneee.ktorin.config

import com.oriooneee.ktorin.room.entities.Request

interface RequestImportantSelector{
    suspend fun selectImportant(request: Request): List<String>
}