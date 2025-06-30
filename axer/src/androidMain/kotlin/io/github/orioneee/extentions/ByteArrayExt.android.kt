package io.github.orioneee.extentions

import android.graphics.BitmapFactory

actual fun ByteArray.isValidImage(): Boolean {
    return try {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(this, 0, this.size, options)
        options.outMimeType?.startsWith("image/") == true
    } catch (e: Exception) {
        false
    }
}