package kotnexlib

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
 * Converts any Object, that implements [Serializable], to its ByteArray representation
 *
 * @return ByteArray representation of this [Serializable]-object.
 */
fun <T : Serializable> T.serializeToByteArray(): ByteArray {
    val baos = ByteArrayOutputStream()
    return ObjectOutputStream(ByteArrayOutputStream()).use {
        it.writeObject(this)
        baos.toByteArray()
    }
}

/**
 * Converts any Base64-String representation back to the original [Serializable]-object. You may created the String with [serializeToString].
 *
 * @return the deserialized object or null on any error.
 */
inline fun <reified T : Serializable> String.deserializeFromStringOrNull(noinline onError: ((Throwable) -> Unit)? = null): T? {
    return tryOrNull(onError = onError) {
        ObjectInputStream(ByteArrayInputStream(fromBase64ToByteArray())).use {
            it.readObject().safeCast<T>()
        }
    }
}

/**
 * Converts any serialized ByteArray representation back to the original [Serializable]-object. You may created the String with [serializeToByteArray].
 *
 * @return the deserialized object or null on any error.
 */
inline fun <reified T : Serializable> ByteArray.deserializeFromByteArrayOrNull(noinline onError: ((Throwable) -> Unit)? = null): T? {
    return tryOrNull(onError = onError) {
        ObjectInputStream(inputStream()).use {
            it.readObject().safeCast<T>()
        }
    }
}