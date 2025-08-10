package io.github.orioneee

import kotlin.time.Duration.Companion.hours

class AxerKtorPluginConfig() {
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
    var requestReducer: (Request) -> Request = { request ->
        request
    }
    var responseReducer: (Response) -> Response = { response ->
        response
    }

    var retentionPeriodInSeconds: Long = 16.hours.inWholeSeconds
    var retentionSizeInBytes: Long = 1024 * 1024 * 100 // 100 MB


    var maxBodySize: Long = 250_000 // ~244 KB
}