import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

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
 * WARNING: Only MD5, SHA-1 and SHA-256 are guaranteed to work on all Java platforms. Amazon Corretto does also support SHA-384 and SHA-512 this!
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

fun String?.isNotNullOrBlank(doThis: (String) -> Unit) {
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
 * Encrypt String with PBKDF2WithHmacSHA1
 *
 * @param encryptionData Data for encryption. Need to be the same as for [decrypt]
 */
fun String.encrypt(encryptionData: EncryptionData): String? { //ResultOf
    try {
        val ivParameterSpec = IvParameterSpec(Base64.getDecoder().decode(encryptionData.iv))


        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val spec = PBEKeySpec(
            encryptionData.secretKey.toCharArray(),
            Base64.getDecoder().decode(encryptionData.salt),
            10000,
            256
        )
        val tmp = factory.generateSecret(spec)
        val secretKey = SecretKeySpec(tmp.encoded, "AES")


        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec)
        return Base64.getEncoder().encodeToString(cipher.doFinal(toByteArray(Charsets.UTF_8)))
    } catch (e: Exception) {
        println("Error while encrypting: $e")
    }
    return null
}


fun String.decrypt(encryptionData: EncryptionData): String? {
    try {


        val ivParameterSpec = IvParameterSpec(Base64.getDecoder().decode(encryptionData.iv))


        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val spec = PBEKeySpec(
            encryptionData.secretKey.toCharArray(),
            Base64.getDecoder().decode(encryptionData.salt),
            10000,
            256
        )
        val tmp = factory.generateSecret(spec)
        val secretKey = SecretKeySpec(tmp.encoded, "AES")


        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec)
        return String(cipher.doFinal(Base64.getDecoder().decode(this)))
    } catch (e: Exception) {
        println("Error while decrypting: $e")
    }
    return null
}

/**
 * Used for encryption/decryption.
 * WARNING: Use other Values for the defaults for security reasons!
 * Hint: Currently I'm not sure if here is a exact length required. If your Keys fail, compare the length of yours with the defaults!
 *
 * @param secretKey Base64-encoded String
 * @param salt Base64-encoded String
 * @param iv Base64-encoded String
 */
data class EncryptionData(
    val secretKey: String = "tK5UTui+DPh8lIlBxya5XVsmeDCoUl6vHhdIESMB6sQ=",
    val salt: String = "QWlGNHNhMTJTQWZ2bGhpV3U=",
    val iv: String = "bVQzNFNhRkQ1Njc4UUFaWA=="
) {
    init {
        //Secret key bestimmte länge? testen!
//        if(salt.length != 16) throw IllegalStateException("Salt length has to be 16!")
//        if(iv.length != 16) throw IllegalStateException("IV length has to be 16!")
    }
}
