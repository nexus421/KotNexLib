

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/**
 * Given string will be covered with stars (*) only the first and last letter will not be changed.
 * e. g. -> "Hello" will be "H***o"
 *
 * For custom start/ending use [coverString]
 *
 * @return covered string
 */
fun String.coverString() = coverString(1, this.length - 2)

/**
 * Der übergebene String wird zwischen start und end durch Sternchen ersetzt.
 * Dient zur Anonymisierung eines Strings
 *
 * @param start ab hier werden Sternchen eingefügt
 * @param end   bis hier werden Sternchen eingefügt
 * @return verschleierten String
 */
fun String.coverString(start: Int, end: Int): String {
    val length = this.length
    if (length < 1) return this
    require(!(end >= length || start < 0)) {
        "Start or end Argument is not allowed! start = $start end = $end"
    }
    val result = toCharArray()
    for (i in 0 until length) {
        if (i in start..end) {
            result[i] = '*'
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
 * WARNING: Only MD5, SHA-1 and SHA-256 are guaranteed to work in all Java platforms. Amazon Coretto does support this!
 * Refer: https://docs.oracle.com/javase/7/docs/api/java/security/MessageDigest.html
 */
enum class HashAlgorithm(val algorithm: String) {
    MD5("MD5"),
    SHA_1("SHA-1"),
    SHA_256("SHA-256"),
    SHA_512("SHA-512"),
}

inline fun String?.ifNullOrBlank(action: () -> Unit) {
    if(isNullOrBlank()) action()
}

inline fun String?.ifNotNullOrBlank(action: String.() -> Unit) {
    if(!isNullOrBlank()) action()
}

fun String?.isNotNullOrBlank() = !isNullOrBlank()

/**
 * If this string is null or blank, the action result is returned. Otherwise this will be returned
 *
 * "".ifNullOrBlank{ "Kadoffe"} -> Results: "Kadoffe"
 */
inline fun String?.ifNullOrBlank(action: () -> String): String {
    return if (isNullOrBlank()) action()
    else this
}

/**
 * Replaces all characters from a String matching "match" until the first character is found, which does not equal "match"
 * Example:
 * String = "0001234"
 * match = '0'
 * Result = "1234"
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

fun String?.embedIfNotNull(before: String = "", after: String = "", fallback: String = "") = if(this == null) fallback else before + this + after

fun String?.isNotNullOrBlank(doThis: (String) -> Unit) {
    if (!isNullOrBlank()) doThis(this)
}

/**
 * Compresses any String. Only usefully for large strings due to overhead. -> More Chars, better compression.
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

fun String.toDate(pattern: String = "dd.MM.yyyy", fallback: Date) = toDateOrNull(pattern) ?: fallback

fun String.toDateOrNull(pattern: String = "dd.MM.yyyy"): Date? {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return try {
        sdf.parse(this)
    } catch (e: Exception) {
        null
    }
}

@Deprecated("Use toDate with fallback or toDateOrNull")
fun String.toDate(pattern: String = "dd.MM.yyyy"): Date? {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return try {
        sdf.parse(this)
    } catch (e: Exception) {
        null
    }
}