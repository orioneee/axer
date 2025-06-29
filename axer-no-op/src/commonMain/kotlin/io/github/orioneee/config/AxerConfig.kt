package io.github.orioneee.config

import io.github.orioneee.domain.requests.Request
import io.github.orioneee.domain.requests.Response


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
