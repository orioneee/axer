package io.github.orioneee.internal.extentions

import io.github.orioneee.internal.domain.requests.data.SavableErrorFull
import io.github.orioneee.internal.domain.requests.data.TransactionFull
import io.ktor.utils.io.charsets.Charset
import io.ktor.utils.io.charsets.Charsets
import io.ktor.utils.io.core.toByteArray


private val UTF8: Charset = Charsets.UTF_8

private fun String.byteLen() = this.toByteArray(UTF8).size

private fun Map<String, String>.byteLen(): Int =
    entries.sumOf { (k, v) -> k.byteLen() + v.byteLen() }

private fun SavableErrorFull.byteLen(): Int = message.byteLen() + stackTrace.byteLen() + name.byteLen()

internal fun TransactionFull.byteSize(): Long {
    var size: Long = 0

    size += Long.SIZE_BYTES
    size += Long.SIZE_BYTES
    size += Int.SIZE_BYTES.takeIf { responseStatus != null } ?: 0
    size += Long.SIZE_BYTES.takeIf { responseTime != null } ?: 0
    size += 1
    size += 1

    size += method.byteLen()
    size += host.byteLen()
    size += path.byteLen()

    size += importantInRequest.sumOf { it.byteLen() }
    size += importantInResponse.sumOf { it.byteLen() }

    size += requestHeaders.byteLen()
    size += responseHeaders.byteLen()

    size += requestBody?.size ?: 0
    size += responseBody?.size ?: 0

    size += error?.byteLen() ?: 0
    return size
}
