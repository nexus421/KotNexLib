package kotnexlib.crypto

import org.bouncycastle.crypto.digests.*
import org.bouncycastle.util.encoders.Hex
import java.security.MessageDigest

/**
 * Hashes a String with the selected [HashAlgorithm]. Default is SHA_256.
 * Based on pure JVM. For more options use [hashBC].
 *
 * SHA-512 is not guaranteed to work on all Java platforms.
 */
fun String.hash(hashAlgorithm: HashAlgorithm = HashAlgorithm.SHA_256) = MessageDigest
    .getInstance(hashAlgorithm.algorithm)
    .digest(toByteArray())
    .fold("") { str, it -> str + "%02x".format(it) }

/**
 * Possible Hash-Algorithms.
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

/**
 * Hashes a String with selected [HashAlgorithmBC] and returns the result as String.
 * Supports more algorithms compared to the default JVM.
 * Everything here is based on BouncyCastle.
 */
fun String.hashBC(hashAlgorithm: HashAlgorithmBC = HashAlgorithmBC.SHA3_256): String {
    val digest = when (hashAlgorithm) {
        HashAlgorithmBC.MD5 -> MD5Digest()
        HashAlgorithmBC.SHA_256 -> SHA256Digest()
        HashAlgorithmBC.SHA_384 -> SHA384Digest()
        HashAlgorithmBC.SHA_512 -> SHA512Digest()
        HashAlgorithmBC.SHA3_256 -> SHA3Digest(256)
        HashAlgorithmBC.SHA3_512 -> SHA3Digest(512)
    }
    val input = toByteArray(Charsets.UTF_8)
    val hash = ByteArray(digest.digestSize)
    digest.update(input, 0, input.size)
    digest.doFinal(hash, 0)
    return Hex.toHexString(hash)
}

enum class HashAlgorithmBC {
    MD5, SHA_256, SHA_384, SHA_512, SHA3_256, SHA3_512
}

/**
 * Iteratively hashes the current string a specified number of times using the given hash algorithm.
 *
 * @param hashAlgorithm The hash algorithm to use for hashing. Defaults to `HashAlgorithm.SHA_256`.
 * @param iterations The number of times the string should be hashed. Must be a positive integer. Defaults to 100,000.
 * @return The resulting hash after the specified number of iterations.
 */
fun String.hashIter(hashAlgorithm: HashAlgorithm = HashAlgorithm.SHA_256, iterations: Int = 100_000) =
    (0..<iterations).fold(this) { acc, _ -> acc.hash(hashAlgorithm) }


