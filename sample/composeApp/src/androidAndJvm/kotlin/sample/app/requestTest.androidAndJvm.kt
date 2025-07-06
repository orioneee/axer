package sample.app

import io.github.orioneee.AxerOkhttpInterceptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

private const val HTTPBIN = "https://httpbin.org"

expect fun getIntercepors(): List<Interceptor>

val client = OkHttpClient.Builder()
    .addInterceptor(
        AxerOkhttpInterceptor.Builder()
            .setRetentionSize(300 * 1024) // 300 KB
            .build()
    )
    .apply {
        getIntercepors().forEach {
            println("Adding interceptor: $it")
            addInterceptor(it)
        }
    }
    .build()


private suspend fun OkHttpClient.safeRequest(
    method: String,
    url: String,
    body: RequestBody? = null
): Result<Response> =
    runCatching {
        withContext(Dispatchers.IO) {
            newCall(
                Request.Builder()
                    .url(url)
                    .method(method, body)
                    .build()
            ).execute()
        }
    }

private val EMPTY_BODY = "".toRequestBody()

private suspend fun OkHttpClient.safeGet(url: String) = safeRequest("GET", url)
private suspend fun OkHttpClient.safeDelete(url: String) = safeRequest("DELETE", url)
private suspend fun OkHttpClient.safePatch(url: String) = safeRequest("PATCH", url, EMPTY_BODY)
private suspend fun OkHttpClient.safePost(url: String) = safeRequest("POST", url, EMPTY_BODY)
private suspend fun OkHttpClient.safePut(url: String) = safeRequest("PUT", url, EMPTY_BODY)

actual suspend fun spamOkHttpMethods() {
    with(client) {
        safeGet(HTTPBIN)
        safeDelete(HTTPBIN)
        safePatch(HTTPBIN)
        safePost(HTTPBIN)
        safePut(HTTPBIN)
    }
}


actual suspend fun spamOkHttpMedia() {
    client.safeGet("$HTTPBIN/image")
    client.safeGet("$HTTPBIN/image/png")
    client.safeGet("$HTTPBIN/image/jpeg")
    client.safeGet("$HTTPBIN/image/svg")
    client.safeGet("$HTTPBIN/image/webp")
}

actual suspend fun spamOkHttpFormatsAndErrors() {
    client.safeGet("$HTTPBIN/html")
    client.safeGet("$HTTPBIN/xml")
    client.safeGet("$HTTPBIN/json")
    client.safeGet("$HTTPBIN/robots.txt")
    client.safeGet("$HTTPBIN/gzip")
    client.safeGet("$HTTPBIN/encoding/utf8")
    client.safeGet("$HTTPBIN/deny")
    client.safeGet("$HTTPBIN/deflate")
    client.safeGet("$HTTPBIN/brotli")

    client.safeGet("https://12345678")
    client.safeGet("abcdefghijklmnopqrstuvwxyz")
}

actual fun isSupportOkhttp() = true