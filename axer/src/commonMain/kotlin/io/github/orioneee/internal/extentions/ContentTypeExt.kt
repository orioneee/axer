package io.github.orioneee.internal.extentions

import io.github.orioneee.internal.domain.requests.formatters.BodyType
import io.ktor.http.ContentType

internal fun ContentType?.toBodyType(): BodyType {
    if (this == null) return BodyType.RAW_TEXT

    val contentType = this.toString().lowercase()

    return when {
        contentType.startsWith("image/") -> BodyType.IMAGE
        contentType.contains("json") -> BodyType.JSON
        else -> BodyType.RAW_TEXT
    }
}