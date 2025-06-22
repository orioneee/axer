package com.oriooneee.axer.config

import com.oriooneee.axer.domain.Request
import com.oriooneee.axer.domain.Response


class AxerConfig(){
    var requestImportantSelector: (Request) -> List<String> = { request ->
        emptyList()
    }
    var responseImportantSelector: (Response) -> List<String> = { response ->
        emptyList()
    }
    var requestFilter: (Request) -> Boolean = { request ->
        true
    }
}
