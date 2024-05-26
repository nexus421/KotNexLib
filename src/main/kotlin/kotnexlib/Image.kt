package kotnexlib

import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

/**
 * Helps editing Images with plain JVM.
 */
data object ImageHelper {

    /**
     * Scales an image to the desired size.
     *
     * It is up to you to check the correct aspect ratio. This method will only scale!
     * On Android you may use Bitmap-Class.
     *
     * @param imageByteArray represents the original image as ByteArray which will be scaled.
     * @param widthPx new width of the image
     * @param heightPx new height of the image
     * @param scaleType Select your desired scale type from [Image]. Defaults to [Image.SCALE_DEFAULT]
     * @param imageType Select the ImageType from [BufferedImage]. Default to [BufferedImage.TYPE_INT_RGB]
     * @param imageFormat Select the image format. Defaults to PNG.
     *
     * @return the scaled image as ByteArray. You may use [ByteArray.asBufferedImageOrNull] to convert the result ByteArray.
     */
    fun scaleImage(
        imageByteArray: ByteArray,
        widthPx: Int,
        heightPx: Int,
        scaleType: Int = Image.SCALE_DEFAULT,
        imageType: Int = BufferedImage.TYPE_INT_RGB,
        imageFormat: String = "png"
    ): ByteArray? {
        return try {
            //Converts the Image-ByteArray to an BufferedImage
            val oriImage =
                requireNotNull(imageByteArray.asBufferedImageOrNull()) { "Error converting ByteArray to BufferedImage" }
            //Creates an BufferedImage with the desired configuration
            val resizedImage = oriImage.getScaledInstance(widthPx, heightPx, scaleType)
            val outImage = BufferedImage(widthPx, heightPx, imageType)
            outImage.graphics.drawImage(resizedImage, 0, 0, null)
            //Writes the scaled image to an ByteArray
            outImage.toByteArray(imageFormat)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

/**
 * Scales an BufferedImage to the desired size.
 *
 * It is up to you to check the correct aspect ratio. This method will only scale!
 *
 * @param widthPx new width of the image
 * @param heightPx new height of the image
 * @param scaleType Select your desired scale type from [Image]. Defaults to [Image.SCALE_DEFAULT]
 * @param imageType Select the ImageType from [BufferedImage]. Default to [BufferedImage.TYPE_INT_RGB]
 * @param imageFormat Select the image format. Defaults to PNG.
 *
 * @return the scaled image as a new BufferedImage
 */
fun BufferedImage.scaleImage(
    widthPx: Int,
    heightPx: Int,
    scaleType: Int = Image.SCALE_DEFAULT,
    imageType: Int = BufferedImage.TYPE_INT_RGB
): BufferedImage? {
    return try {
        //Creates an BufferedImage with the desired configuration
        val resizedImage = getScaledInstance(widthPx, heightPx, scaleType)
        val outImage = BufferedImage(widthPx, heightPx, imageType)
        outImage.graphics.drawImage(resizedImage, 0, 0, null)
        outImage
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * Converts this ByteArray to its [BufferedImage] representation or null if an error occurs.
 * @return This ByteArray as [BufferedImage] or null.
 */
fun ByteArray.asBufferedImageOrNull(): BufferedImage? = inputStream().use { kotnexlib.tryOrNull { ImageIO.read(it) } }

/**
 * Converts this BufferedImage to its ByteArray representation or null if an error occurs.
 *
 * @param imageFormat the image format. Defaults to PNG.
 *
 * @return The image as ByteArray or null.
 */
fun BufferedImage.toByteArray(imageFormat: String = "png"): ByteArray? {
    return ByteArrayOutputStream().use {
        kotnexlib.tryOrNull {
            ImageIO.write(this, imageFormat, it)
            it.toByteArray()
        }
    }
}