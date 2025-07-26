package io.github.orioneee

import io.github.orioneee.AxerOkhttpInterceptor
import io.github.orioneee.domain.requests.Request
import okhttp3.Interceptor
import okhttp3.Response
import kotlin.time.Duration.Companion.hours

class AxerOkhttpInterceptor constructor() : Interceptor {

    class Builder() {
        fun setRequestImportantSelector(selector: (Request) -> List<String>) = apply {}
        fun setRequestReducer(reducer: (Request) -> Request) = apply {}
        fun setResponseFilter(filter: (io.github.orioneee.domain.requests.Response) -> Boolean) =
            apply {}

        fun setResponseImportantSelector(selector: (io.github.orioneee.domain.requests.Response) -> List<String>) =
            apply {}

        fun setRequestFilter(filter: (Request) -> Boolean) = apply {}
        fun setResponseReducer(reducer: (io.github.orioneee.domain.requests.Response) -> io.github.orioneee.domain.requests.Response) =
            apply {}

        fun setRetentionTime(seconds: Long) = apply {}
        fun setRetentionSize(sizeInBytes: Long) = apply {}

        fun setMaxBodySize(sizeInBytes: Long) = apply {}

        fun build() = AxerOkhttpInterceptor()
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(chain.request())
    }
}
