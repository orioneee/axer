package sample.app


import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put

private const val HTTPBIN = "https://httpbin.org"

internal suspend fun spamKtorMethods(client: HttpClient) = with(client) {
    runCatching { get(HTTPBIN) }
    runCatching { delete(HTTPBIN) }
    runCatching { patch(HTTPBIN) }
    runCatching { post(HTTPBIN) }
    runCatching { put(HTTPBIN) }
}

internal suspend fun spamKtorMedia(client: HttpClient) = with(client) {
    runCatching { get("$HTTPBIN/image") }
    runCatching { get("$HTTPBIN/image/png") }
    runCatching { get("$HTTPBIN/image/jpeg") }
    runCatching { get("$HTTPBIN/image/svg") }
    runCatching { get("$HTTPBIN/image/webp") }
}

internal suspend fun spamKtorFormatsAndErrors(client: HttpClient) = with(client) {
    runCatching { get("$HTTPBIN/html") }
    runCatching { get("$HTTPBIN/xml") }
    runCatching { get("$HTTPBIN/json") }
    runCatching { get("$HTTPBIN/robots.txt") }
    runCatching { get("$HTTPBIN/gzip") }
    runCatching { get("$HTTPBIN/encoding/utf8") }
    runCatching { get("$HTTPBIN/deny") }
    runCatching { get("$HTTPBIN/deflate") }
    runCatching { get("$HTTPBIN/brotli") }

    runCatching { get("https://12345678") }
    runCatching { get("abcdefghijklmnopqrstuvwxyz") }
}

internal suspend fun spamKtor(client: HttpClient) {
    spamKtorMethods(client)
    spamKtorMedia(client)
    spamKtorFormatsAndErrors(client)
}

expect fun isSupportOkhttp(): Boolean
expect suspend fun spamOkHttpMethods()
expect suspend fun spamOkHttpMedia()
expect suspend fun spamOkHttpFormatsAndErrors()

suspend fun spamOkHttp(client: HttpClient) {
    spamOkHttpMethods()
    spamOkHttpMedia()
    spamOkHttpFormatsAndErrors()
}

