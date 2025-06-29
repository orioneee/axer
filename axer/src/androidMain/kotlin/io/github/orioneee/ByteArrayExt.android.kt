package io.github.orioneee

actual fun ByteArray.isValidImage(): Boolean {
    return try {
        val options = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
        android.graphics.BitmapFactory.decodeByteArray(this, 0, this.size, options)
        options.outMimeType?.startsWith("image/") == true
    } catch (e: Exception) {
        false
    }
}