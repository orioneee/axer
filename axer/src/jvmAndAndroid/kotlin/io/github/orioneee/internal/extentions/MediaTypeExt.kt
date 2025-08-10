package io.github.orioneee.internal.extentions

import io.github.orioneee.internal.domain.requests.formatters.BodyType
import okhttp3.MediaType

fun MediaType?.toBodyType(): BodyType {
    if (this == null) return BodyType.RAW_TEXT

    val subtype = this.subtype.lowercase()
    return when {
        this.type == "image" -> BodyType.IMAGE
        subtype.contains("json") -> BodyType.JSON
        else -> BodyType.RAW_TEXT
    }
}