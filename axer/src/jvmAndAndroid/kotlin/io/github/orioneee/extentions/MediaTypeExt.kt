package io.github.orioneee.extentions

import io.github.orioneee.domain.requests.formatters.BodyType
import okhttp3.MediaType

fun MediaType?.toBodyType(): BodyType {
    if (this == null) return BodyType.RAW_TEXT

    val subtype = this.subtype.lowercase()
    return when {
        this.type == "image" -> BodyType.IMAGE
        subtype.contains("json") -> BodyType.JSON
        subtype.contains("html") -> BodyType.HTML
        subtype.contains("xml") -> BodyType.XML
        subtype.contains("css") -> BodyType.CSS
        subtype.contains("javascript") || subtype.contains("ecmascript") || subtype == "js" -> BodyType.JAVASCRIPT
        else -> BodyType.RAW_TEXT
    }
}