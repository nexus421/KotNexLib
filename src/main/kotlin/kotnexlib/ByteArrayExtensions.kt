package kotnexlib

import java.util.*

/**
 * Shortcut to convert any ByteArray to its Base64-String representation.
 */
fun ByteArray.toBase64(): String = Base64.getEncoder().encodeToString(this)