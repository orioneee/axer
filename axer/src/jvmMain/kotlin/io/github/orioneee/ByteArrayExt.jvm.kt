package io.github.orioneee

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
