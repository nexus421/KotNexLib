package kotnexlib

import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

/**
 * Converts any Object, that implements [Serializable], to its Base64-String representation
 *
 * @return Base64-String representation of this [Serializable]-object.
 */
fun <T : Serializable> T.serializeToString(): String = serializeToByteArray().toBase64()

/**
 * Converts any Object, that implements [Serializable], to its ByteArray representation
 *
 * @return ByteArray representation of this [Serializable]-object.
 */
fun <T : Serializable> T.serializeToByteArray(): ByteArray {
    val baos = ByteArrayOutputStream()
    return ObjectOutputStream(baos).use {
        it.writeObject(this)
        baos.toByteArray()
    }
}

/**
 * Converts any Base64-String representation back to the original [Serializable]-object. You may create the String with [serializeToString].
 *
 * @param onError Optional callback invoked when deserialization fails.
 * @return the deserialized object or null if deserialization fails.
 */
inline fun <reified T : Serializable> String.deserializeFromStringOrNull(noinline onError: ((Throwable) -> Unit)? = null): T? =
    tryOrNull(onError) { fromBase64ToByteArray().deserializeFromByteArrayOrNull<T>(onError) }

/**
 * Converts any serialized ByteArray representation back to the original [Serializable]-object. You may create the ByteArray with [serializeToByteArray].
 *
 * @param onError Optional callback invoked when deserialization fails.
 * @return the deserialized object or null if deserialization fails.
 */
inline fun <reified T : Serializable> ByteArray.deserializeFromByteArrayOrNull(noinline onError: ((Throwable) -> Unit)? = null): T? =
    tryOrNull(onError) {
        ObjectInputStream(inputStream()).use {
            it.readObject().cast<T>()
        }
    }

/**
 * Converts any Base64-String representation back to the original [Serializable]-object as a [Result].
 */
inline fun <reified T : Serializable> String.deserializeFromString(): Result<T> =
    runCatching { fromBase64ToByteArray().deserializeFromByteArray<T>().getOrThrow() }

/**
 * Converts any serialized ByteArray representation back to the original [Serializable]-object as a [Result].
 */
inline fun <reified T : Serializable> ByteArray.deserializeFromByteArray(): Result<T> =
    runCatching {
        ObjectInputStream(inputStream()).use {
            it.readObject().cast<T>()
        }
    }
