package io.github.orioneee.extentions

import io.github.orioneee.domain.requests.formatters.BodyType
import io.ktor.http.ContentType

fun ContentType?.toBodyType(): BodyType {
    if (this == null) return BodyType.RAW_TEXT

    val contentType = this.toString().lowercase()

    return when {
        contentType.startsWith("image/") -> BodyType.IMAGE
        contentType.contains("json") -> BodyType.JSON
        contentType.contains("html") -> BodyType.HTML
        contentType.contains("xml") -> BodyType.XML
        contentType.contains("css") -> BodyType.CSS
        contentType.contains("javascript") || contentType.contains("ecmascript") || contentType.endsWith(
            "/js"
        ) -> BodyType.JAVASCRIPT

        else -> BodyType.RAW_TEXT
    }
}