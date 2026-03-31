package io.github.orioneee.internal.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.core.content.FileProvider
import io.github.orioneee.internal.koin.IsolatedContext
import org.koin.core.component.inject
import java.io.File

actual suspend fun copyImageToClipboard(imageBytes: ByteArray) {
    val context: Context by IsolatedContext.koin.inject()
    val file = File(context.cacheDir, "axer_clipboard_image.png")
    file.writeBytes(imageBytes)

    val authority = "${context.packageName}.io.orioneee.axer.provider"
    val uri = FileProvider.getUriForFile(context, authority, file)

    val clip = ClipData.newUri(context.contentResolver, "Image", uri)
    val clipboardManager =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboardManager.setPrimaryClip(clip)
}
