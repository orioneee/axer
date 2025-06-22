package com.oriooneee.ktorin.config

import com.oriooneee.ktorin.domain.Request
import com.oriooneee.ktorin.domain.Response


class KtorinConfig(){

    var requestImportantSelector: RequestImportantSelector = object : RequestImportantSelector {
        override suspend fun selectImportant(request: Request): List<String> {
            return emptyList()
        }
    }
    var responseImportantSelector: ResponseImportantSelector = object : ResponseImportantSelector {
        override suspend fun selectImportant(response: Response): List<String> {
            return emptyList()
        }
    }
}
