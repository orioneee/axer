package com.oriooneee.axer

import com.oriooneee.axer.domain.Request
import okhttp3.Interceptor
import okhttp3.Response

class AxerOkhttpInterceptor private constructor(
    private val requestImportantSelector: (Request) -> List<String>,
    private val responseImportantSelector: (com.oriooneee.axer.domain.Response) -> List<String>,
    private val requestFilter: (Request) -> Boolean,
) : Interceptor {

    class Builder() {
        private var requestImportantSelector: (Request) -> List<String> = { request ->
            emptyList()
        }

        private var responseImportantSelector: (com.oriooneee.axer.domain.Response) -> List<String> =
            { response ->
                emptyList()
            }

        private var requestFilter: (Request) -> Boolean = { request ->
            true
        }

        fun setRequestImportantSelector(selector: (Request) -> List<String>) = apply {
            this.requestImportantSelector = selector
        }

        fun setResponseImportantSelector(selector: (com.oriooneee.axer.domain.Response) -> List<String>) =
            apply {
                this.responseImportantSelector = selector
            }

        fun setRequestFilter(filter: (Request) -> Boolean) = apply {
            this.requestFilter = filter
        }

        fun build() = AxerOkhttpInterceptor(
            requestImportantSelector = requestImportantSelector,
            responseImportantSelector = responseImportantSelector,
            requestFilter = requestFilter
        )
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(chain.request())
    }
}
