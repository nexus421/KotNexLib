package kotnexlib

import fromBase64ToByteArray
import safeCast
import tryOrNull
import java.io.*

/**
 * Converts any Object, that implements [Serializable], to its Base64-String representation
 *
 * @return Base64-String representation of this [Serializable]-object.
 */
fun <T : Serializable> T.serializeToString(): String {
    val baos = ByteArrayOutputStream()
    return ObjectOutputStream(baos).use {
        it.writeObject(this)
        baos.toByteArray().toBase64()
    }
}

/**
 * Converts any Base64-String representation back to the original [Serializable]-object. You may created the String with [serializeToString].
 *
 * @return the deserialized object or null on any error.
 */
inline fun <reified T : Serializable> String.deserializeFromStringOrNull(noinline onError: ((Throwable) -> Unit)? = null): T? {
    return tryOrNull(onError = onError) {
        val bais = ByteArrayInputStream(fromBase64ToByteArray())
        ObjectInputStream(bais).use {
            it.readObject().safeCast<T>()
        }
    }
}