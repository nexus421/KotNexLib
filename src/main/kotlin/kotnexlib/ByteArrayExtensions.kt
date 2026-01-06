package kotnexlib

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/**
 * Shortcut to convert any ByteArray to its Base64-String representation.
 */
fun ByteArray.toBase64(): String = Base64.getEncoder().encodeToString(this)

/**
 * Compresses a nullable or empty `ByteArray` using GZIP compression and encodes the result in Base64.
 * If the input is `null` or empty, it returns the same input.
 * In case of an error during compression, `null` is returned.
 *
 * @return A `ByteArray` containing the Base64-encoded compressed data, or the original input if
 *         it is `null` or empty, or `null` if an exception occurs.
 */
fun ByteArray?.compress(): ByteArray? = try {
    if (this == null || isEmpty()) {
        this
    } else {
        val out = ByteArrayOutputStream()
        GZIPOutputStream(out).use {
            it.write(this)
        }
        Base64.getEncoder().encode(out.toByteArray())
    }
} catch (e: Exception) {
    e.printStackTrace()
    null
}

/**
 * Decompresses the byte array, expecting it to be Base64 encoded and GZIP compressed. (Should be compressed with [compress])
 * If the input array is null, empty, or decompression fails, the function returns null.
 *
 * @return The decompressed byte array, or null if the input is null, empty, or an error occurs during decompression.
 */
fun ByteArray?.decompress(): ByteArray? = try {
    if (this == null || isEmpty()) {
        this
    } else {
        GZIPInputStream(ByteArrayInputStream(Base64.getDecoder().decode(this))).use {
            it.readBytes()
        }
    }
} catch (e: Exception) {
    e.printStackTrace()
    null
}