package io.github.orioneee.internal.utils

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIImage
import platform.UIKit.UIPasteboard

@OptIn(ExperimentalForeignApi::class)
actual suspend fun copyImageToClipboard(imageBytes: ByteArray) {
    val nsData = imageBytes.usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = imageBytes.size.toULong())
    }
    val image = UIImage.imageWithData(nsData) ?: return
    UIPasteboard.generalPasteboard.image = image
}
