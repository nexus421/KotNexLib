package kotnexlib

/**
 * This gets the name of the current Class.
 * I use this for my Logs. So I always have an identical TAG to use
 */
inline val Any.TAG: String
    get() = javaClass.simpleName
/**
 * Cast this to C. Force-Cast. Throws exception on error.
 */
inline fun <reified C> Any.cast(): C = this as C

/**
 * Casts this to C. If the cast failed, it will return null.
 */
inline fun <reified C> Any.safeCast(): C? = this as? C

/**
 * Cast this to C and calls [block] with the casted object.
 * Throws exception if cast is not possible.
 */
inline fun <reified C, R> Any.letCast(block: (C) -> R): R = (this as C).let(block)

@OptIn(ExperimentalUnsignedTypes::class)
fun ByteArray.toHexString() = asUByteArray().joinToString(separator = "") { it.toString(16).padStart(2, '0') }


