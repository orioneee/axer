package com.oriooneee.ktorin.config


class KtorinConfig(){
    var requestImportantSelector: RequestImportantSelector = object : RequestImportantSelector {
        override suspend fun selectImportant(request: com.oriooneee.ktorin.room.entities.Request): List<String> {
            return emptyList()
        }
    }
    var responseImportantSelector: ResponseImportantSelector = object : ResponseImportantSelector {
        override suspend fun selectImportant(response: com.oriooneee.ktorin.room.entities.Response): List<String> {
            return emptyList()
        }
    }
}
