package io.github.orioneee.internal.utils

import java.awt.Image
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

actual suspend fun copyImageToClipboard(imageBytes: ByteArray) {
    val image = ImageIO.read(ByteArrayInputStream(imageBytes)) ?: return
    val transferable = object : Transferable {
        override fun getTransferDataFlavors(): Array<DataFlavor> =
            arrayOf(DataFlavor.imageFlavor)

        override fun isDataFlavorSupported(flavor: DataFlavor): Boolean =
            flavor == DataFlavor.imageFlavor

        override fun getTransferData(flavor: DataFlavor): Any {
            if (flavor == DataFlavor.imageFlavor) return image
            throw UnsupportedFlavorException(flavor)
        }
    }
    Toolkit.getDefaultToolkit().systemClipboard.setContents(transferable, null)
}
