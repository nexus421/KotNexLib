package kotnexlib.crypto

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Utilities for computing and verifying HMAC (Hash-based Message Authentication Code) signatures.
 *
 * HMAC is the standard building block for API request signing and payload integrity checks
 * (e.g. GitHub/Stripe webhook signatures, AWS SigV4, JWT `HS256`).
 */
object HMAC {

    /**
     * The supported HMAC algorithms, mapped to their JCE algorithm name.
     */
    enum class Algorithm(val jvmName: String) {
        SHA256("HmacSHA256"),
        SHA512("HmacSHA512"),
        SHA1("HmacSHA1")
    }

    /**
     * Computes the HMAC of [data] with [key].
     *
     * @param data the raw data to sign.
     * @param key the secret key. Must not be empty.
     * @param algorithm the HMAC algorithm to use. Default: SHA256.
     * @return the HMAC as a byte array.
     */
    fun compute(data: ByteArray, key: ByteArray, algorithm: Algorithm = Algorithm.SHA256): ByteArray {
        require(key.isNotEmpty()) { "The key must not be empty!" }
        val mac = Mac.getInstance(algorithm.jvmName)
        mac.init(SecretKeySpec(key, algorithm.jvmName))
        return mac.doFinal(data)
    }

    /**
     * Computes the HMAC of [data] with [key] and returns it as a lowercase hex string.
     *
     * @param data the text to sign (UTF-8).
     * @param key the secret key (UTF-8). Must not be empty.
     * @param algorithm the HMAC algorithm to use. Default: SHA256.
     * @return the HMAC as a lowercase hex string.
     */
    fun computeHex(data: String, key: String, algorithm: Algorithm = Algorithm.SHA256): String {
        val hmacBytes = compute(data.toByteArray(Charsets.UTF_8), key.toByteArray(Charsets.UTF_8), algorithm)
        return hmacBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Performs a constant-time comparison of two signatures, to protect against timing attacks.
     * Always use this (never [ByteArray.contentEquals] or `==`) to compare a received signature
     * against an expected one.
     *
     * @param signatureA first signature.
     * @param signatureB second signature.
     * @return `true` if both signatures are identical.
     */
    fun verifyConstantTime(signatureA: ByteArray, signatureB: ByteArray): Boolean =
        java.security.MessageDigest.isEqual(signatureA, signatureB)
}

/**
 * @param secret the secret key (UTF-8). Must not be empty.
 * @return the HMAC-SHA256 of this string as a lowercase hex string.
 */
fun String.hmacSha256(secret: String): String = HMAC.computeHex(this, secret, HMAC.Algorithm.SHA256)
