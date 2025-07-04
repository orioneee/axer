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
    private val responseReducer: (io.github.orioneee.domain.requests.Response) -> io.github.orioneee.domain.requests.Response,
    private val requestMaxAgeInSeconds: Long
) : Interceptor {
    class Builder() {
        private var requestImportantSelector: (Request) -> List<String> = { emptyList() }
        private var responseImportantSelector: (io.github.orioneee.domain.requests.Response) -> List<String> =
            { emptyList() }
        private var requestFilter: (Request) -> Boolean = { true }
        private var requestReducer: (Request) -> Request = { it }
        private var responseFilter: (io.github.orioneee.domain.requests.Response) -> Boolean =
            { true }
        private var responseReducer: (io.github.orioneee.domain.requests.Response) -> io.github.orioneee.domain.requests.Response =
            { it }
        private var requestMaxAgeInSeconds: Long = 60 * 60 * 1

        fun setRequestImportantSelector(selector: (Request) -> List<String>) = apply {
            this.requestImportantSelector = selector
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


        fun setResponseReducer(reducer: (io.github.orioneee.domain.requests.Response) -> io.github.orioneee.domain.requests.Response) =
            apply {
                this.responseReducer = reducer
            }

        fun setRetentionTime(seconds: Long) = apply {
            this.requestMaxAgeInSeconds = seconds
        }

        fun build() = AxerOkhttpInterceptor(
            requestImportantSelector = requestImportantSelector,
            responseImportantSelector = responseImportantSelector,
            requestFilter = requestFilter,
            responseFilter = responseFilter,
            requestReducer = requestReducer,
            responseReducer = responseReducer,
            requestMaxAgeInSeconds = requestMaxAgeInSeconds
        )
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(chain.request())
    }
}
