package io.github.orioneee

import io.github.orioneee.domain.requests.Request
import okhttp3.Interceptor
import okhttp3.Response

class AxerOkhttpInterceptor private constructor(
    private val requestImportantSelector: (Request) -> List<String>,
    private val responseImportantSelector: (io.github.orioneee.domain.requests.Response) -> List<String>,
    private val requestFilter: (Request) -> Boolean,
    private val responseFilter: (io.github.orioneee.domain.requests.Response) -> Boolean,
    private val requestReducer: (Request) -> Request = { request -> request },
    private val responseReducer: (io.github.orioneee.domain.requests.Response) -> io.github.orioneee.domain.requests.Response
) : Interceptor {

    class Builder() {
        private var requestImportantSelector: (Request) -> List<String> = { request ->
            emptyList()
        }

        private var responseImportantSelector: (io.github.orioneee.domain.requests.Response) -> List<String> =
            { response ->
                emptyList()
            }

        private var requestFilter: (Request) -> Boolean = { request ->
            true
        }

        fun setRequestImportantSelector(selector: (Request) -> List<String>) = apply {
            this.requestImportantSelector = selector
        }

        private var requestReducer: (Request) -> Request = { request ->
            request
        }
        private var responseFilter: (io.github.orioneee.domain.requests.Response) -> Boolean =
            { response ->
                true
            }

        fun setRequestReducer(reducer: (Request) -> Request) = apply {
            this.requestReducer = reducer
        }

        fun setResponseFilter(filter: (io.github.orioneee.domain.requests.Response) -> Boolean) =
            apply {
                this.responseFilter = filter
            }

        fun setResponseImportantSelector(selector: (io.github.orioneee.domain.requests.Response) -> List<String>) =
            apply {
                this.responseImportantSelector = selector
            }

        fun setRequestFilter(filter: (Request) -> Boolean) = apply {
            this.requestFilter = filter
        }

        private var responseReducer: (io.github.orioneee.domain.requests.Response) -> io.github.orioneee.domain.requests.Response =
            { response ->
                response
            }

        fun setResponseReducer(reducer: (io.github.orioneee.domain.requests.Response) -> io.github.orioneee.domain.requests.Response) =
            apply {
                this.responseReducer = reducer
            }

        fun build() = AxerOkhttpInterceptor(
            requestImportantSelector = requestImportantSelector,
            responseImportantSelector = responseImportantSelector,
            requestFilter = requestFilter,
            responseFilter = responseFilter,
            requestReducer = requestReducer,
            responseReducer = responseReducer
        )
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(chain.request())
    }
}
