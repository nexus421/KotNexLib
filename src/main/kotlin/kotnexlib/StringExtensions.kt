package kotnexlib

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/**
 * Given string will be covered with [coverChar] from [start] to [end].
 * Default uses '*' and covers all characters except the first and last one.
 *
 * @param start start index to start covering. Default is 1.
 * @param end   end index to end covering. Default is [String.length] - 2
 *
 * @return (partly) covered string. If the given index is to high/low, the original string will be returned.
 */
fun String.coverString(start: Int = 1, end: Int = length - 2, coverChar: Char = '*'): String {
    require(start < end) { "Start has to be lower than end!" }
    if (isEmpty()) return this
    if ((end >= length || start < 0).not()) return this

    val result = toCharArray()
    for (i in indices) {
        if (i in start..end) {
            result[i] = coverChar
        }
    }
    return String(result)
}

fun String.withNewLine() = this + "\n"

/**
 * Hashes a String with the selected HashAlgorithm. Default is SHA_256.
 *
 * SHA-512 is not guaranteed to work on all Java platforms.
 */
fun String.hash(hashAlgorithm: HashAlgorithm = HashAlgorithm.SHA_256) = MessageDigest
    .getInstance(hashAlgorithm.algorithm)
    .digest(toByteArray())
    .fold("") { str, it -> str + "%02x".format(it) }

/**
 * Possible Hash-Algorithms
 *
 * WARNING: Only MD5, SHA-1 and SHA-256 are guaranteed to work on all Java platforms. Amazon Corretto does also support SHA-384 and SHA-512!
 * Refer: https://docs.oracle.com/javase/7/docs/api/java/security/MessageDigest.html
 * Amazon Corretto: https://github.com/corretto/amazon-corretto-crypto-provider/blob/main/README.md
 */
enum class HashAlgorithm(val algorithm: String) {
    MD5("MD5"),
    SHA_1("SHA-1"),
    SHA_256("SHA-256"),
    SHA_384("SHA-384"),
    SHA_512("SHA-512"),
}

inline fun String?.ifNullOrBlankDo(action: () -> Unit) {
    if(isNullOrBlank()) action()
}

fun String?.isNotNullOrBlank() = !isNullOrBlank()

/**
 * If this string is null or blank, the action result is returned. Otherwise this will be returned
 *
 * "".ifNullOrBlank{ "Kadoffe"} -> Results: "Kadoffe"
 */
inline fun String?.ifNullOrBlank(action: () -> String): String = if (isNullOrBlank()) action() else this

/**
 * Replaces all characters from a String matching [match] until the first character is found, which does not equal "match"
 * Example:
 * String = "00012340"
 * match = '0'
 * Result = "12340"
 *
 * @param match Character wich should be removed from the start
 * @return String which does not start with "match"
 */
fun String.replaceAllMatchingStart(match: Char): String {
    toCharArray().forEachIndexed { index, c ->
        if (c != match) return substring(index)
    }
    return this
}

/**
 * @return If this String is not null, the String will be returned linke "[before]this[after]". Otherwise fallback will be returned.
 */
fun String?.embedIfNotNull(before: String = "", after: String = "", fallback: String = "") = if(this == null) fallback else before + this + after

inline fun String?.ifNotNullOrBlank(doThis: (String) -> Unit) {
    if (!isNullOrBlank()) doThis(this)
}

/**
 * Compresses any String. Only useful for large strings due to overhead. -> More Chars, better compression.
 *
 * @return Compressed String as Base64 or null, if an error occurred
 */
fun String?.compress(): String? = try {
    if (isNullOrBlank()) {
        this
    } else {
        val out = ByteArrayOutputStream()
        GZIPOutputStream(out).use {
            it.write(toByteArray())
        }
        String(Base64.getEncoder().encode(out.toByteArray()))
    }
} catch (e: Exception) {
    e.printStackTrace()
    null
}

/**
 * Decompress a Base64 based String, which was compressed with [compress].
 *
 * @return decompressed String or null when an error occurred.
 */
fun String?.decompress(): String? = try {
    if (isNullOrBlank()) {
        this
    } else {
        GZIPInputStream(ByteArrayInputStream(Base64.getDecoder().decode(this))).use {
            String(it.readBytes())
        }
    }
} catch (e: Exception) {
    e.printStackTrace()
    null
}

/**
 * Checks if this string is contained in that string or if that string is contained in this string
 *
 * @param that other String to check for containment
 * @param ignoreCase true to ignore character case when comparing strings. By default false.
 */
fun String.crossContains(that: String, ignoreCase: Boolean = false) = this.contains(that, ignoreCase) || that.contains(this, ignoreCase)

val String.isDigitOnly: Boolean
    get() = matches(Regex("^\\d*\$"))

val String.isAlphabeticOnly: Boolean
    get() = matches(Regex("^[a-zA-Z]*\$"))

val String.isAlphanumericOnly: Boolean
    get() = matches(Regex("^[a-zA-Z\\d]*\$"))


/**
 * Converts this String to a [Date] with the given [pattern].
 *
 * @param pattern used to convert the String to a Date.
 * @param fallback Date to return when conversion failed.
 */
fun String.toDate(pattern: String = "dd.MM.yyyy", fallback: Date) = toDateOrNull(pattern) ?: fallback

/**
 * Converts this String to a [Date] with the given [pattern] or null if the conversion failed.
 */
fun String.toDateOrNull(pattern: String = "dd.MM.yyyy"): Date? {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return kotnexlib.tryOrNull { sdf.parse(this) }
}

/**
 * Hex-String to Decimal-Int
 */
fun String.hexToInt() = Integer.parseInt(this, 16)

/**
 * Checks if this string contains all elements from [list]
 *
 * @param list elements required in this String.
 * @param ignoreCase if true, the strings a compared with ignore case
 */
fun String.containsAll(list: List<String>, ignoreCase: Boolean = true): Boolean {
    for (element in list) {
        if (contains(element, ignoreCase).not()) return false
    }
    return true
}

/**
 * Checks if this String contains at least one element from [list].
 *
 * @param list elements to check if one of these are contained in this string
 * @param ignoreCase if true, the strings will be compared using ignore case
 *
 * @return true, if at least one element from [list] is contained.
 */
fun String.containsOneOf(list: List<String>, ignoreCase: Boolean = false): Boolean {
    list.forEach {
        if (contains(it, ignoreCase)) return true
    }
    return false
}

/**
 * Checks if this String contains at least one element from [list].
 * Example: "Hello World!" with ["Banana", "World", "Car"] would return "World"
 *
 * @param list elements to check if one of these are contained in this string
 * @param ignoreCase if true, the strings will be compared using ignore case
 *
 * @return the first matching String from [list] or null
 */
fun String.containsOneOfAndGet(list: List<String>, ignoreCase: Boolean = false): String? {
    list.forEach {
        if (contains(it, ignoreCase)) return it
    }
    return null
}

/**
 * Sets this String to the clipboard of the system.
 * This may not work on Android!
 */
fun String.copyToClipboard() {
    tryOrNull {
        val selection = StringSelection(this)
        Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, selection)
    }
}

/**
 * Shortcut to convert any String to its Base64 representation.
 */
fun String.toBase64(): String = Base64.getEncoder().encodeToString(toByteArray())

/**
 * Shortcut to convert any Base64-String back to its String representation.
 * @return the String representation or throws an exception on any error.
 */
fun String.fromBase64() = String(Base64.getDecoder().decode(this))

/**
 * Shortcut to convert any Base64-String back to its ByteArray representation.
 * @return the ByteArray representation or throws an exception on any error.
 */
fun String.fromBase64ToByteArray(): ByteArray = Base64.getDecoder().decode(this)

/**
 * Shortcut to convert any Base64-String back to its ByteArray representation.
 * @return the ByteArray representation or null on any error.
 */
fun String.fromBase64ToByteArrayOrNull() = tryOrNull { Base64.getDecoder().decode(this) }

/**
 * Shortcut to convert any Base64-String back to its String representation.
 * @return the String representation or null on any error.
 */
fun String.fromBase64OrNull() = tryOrNull { String(Base64.getDecoder().decode(this)) }

/**
 * Adds [separator] after each character from this string.
 * With " " separator:
 * input = "abcd"
 * output = "a b c d"
 */
fun String.splitEachCharBy(separator: String = " ") = toCharArray().joinToString(separator)