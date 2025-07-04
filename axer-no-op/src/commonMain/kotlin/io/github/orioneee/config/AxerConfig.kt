package io.github.orioneee.config

import io.github.orioneee.domain.requests.Request
import io.github.orioneee.domain.requests.Response


class AxerKtorPluginConfig(){
    var requestImportantSelector: (Request) -> List<String> = { request ->
        emptyList()
    }
    var responseImportantSelector: (Response) -> List<String> = { response ->
        emptyList()
    }
    var requestFilter: (Request) -> Boolean = { request ->
        true
    }
    var responseFilter: (Response) -> Boolean = { response ->
        true
    }
    var requestReducer : (Request) -> Request = { request ->
        request
    }
    var responseReducer : (Response) -> Response = { response ->
        response
    }
}
