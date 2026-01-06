package kotnexlib

import io.ktor.util.*
import java.nio.CharBuffer

/**
 * Converts the character array to a UTF-8 encoded byte array, applies the provided operation,
 * and securely wipes the character array and the byte array after use to prevent sensitive data leakage.
 *
 * @param byteArray A function that receives the UTF-8 encoded byte array as input for processing.
 * @return A [Result] that encapsulates the result of the provided operation or the exception if it fails.
 */
fun CharArray.useAndWipe(byteArray: (ByteArray) -> Unit): Result<Unit> {
    val convertedByteBuffer = Charsets.UTF_8.encode(CharBuffer.wrap(this))
    val convertedByteArray = convertedByteBuffer.moveToByteArray()
    fill('\u0000')
    convertedByteBuffer.array().fill(0)
    return runCatching { byteArray(convertedByteArray) }.also {
        convertedByteArray.fill(0)
    }
}