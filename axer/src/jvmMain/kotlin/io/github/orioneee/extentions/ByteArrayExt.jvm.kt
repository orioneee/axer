package io.github.orioneee.extentions

// JVM only
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

actual fun ByteArray.isValidImage(): Boolean {
    return try {
        val inputStream = this.inputStream()
        val image: BufferedImage? = ImageIO.read(inputStream)
        image != null
    } catch (e: Exception) {
        false
    }
}
