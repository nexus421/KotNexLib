package kotnexlib.crypto

import kotnexlib.crypto.Argon2Helper.hash
import kotnexlib.fromBase64
import kotnexlib.fromBase64ToByteArray
import kotnexlib.toBase64
import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters
import java.security.MessageDigest

fun main() {
    val hash = hash("password".toCharArray(), iterations = 10)
    println(hash)
    println(Argon2Helper.verify("password".toCharArray(), hash))
}

object Argon2Helper {

    private const val DEFAULT_HASH_LENGTH = 32

    /**
     * Generates a secure Argon2id hash for passwords specifically tailored for mobile devices.
     * This method uses predefined parameters optimized for minimizing resource usage while maintaining security.
     * Calls [hash] internally with optimized parameters.
     *
     * @param password The password to hash. Uses CharArray to enable memory clearing after use for better security.
     * @return A PHC (Password Hashing Competition) formatted string containing the Argon2id hash and all necessary parameters for verification as base64.
     */
    fun hashForMobileDevices(password: CharArray): String = hash(password, iterations = 4, memoryKb = 16384)

    /**
     * Generates a secure Argon2id hash for the given password using customizable parameters.
     * The output is formatted according to the PHC string format, containing all parameters needed for verification.
     *
     * Format: $argon2id$v=19$m=<memory>,t=<iterations>,p=<parallelism>$<saltHex>$<hashHex>
     *
     * @param password The password to hash. Use CharArray to allow clearing memory after use.
     * @param iterations (T-Cost): Number of passes over the memory. Higher = slower = more secure.
     * Minimum allowed: 1. Default: 3.
     * @param memoryKb (M-Cost): Amount of memory in KiB to use. This is the main defense against GPUs/ASICs.
     * Minimum allowed: 8192 (8 MB). Default: 65536 (64 MB).
     * @param parallelism (Lanes): Number of threads and compute lanes. Default: 1.
     * Warning: Only increase if you are sure the host machine handles multi-threading well.
     * @return A self-contained string including algorithm, parameters, salt, and hash as Base64.
     */
    fun hash(
        password: CharArray,
        iterations: Int = 3,
        memoryKb: Int = 65536,
        parallelism: Int = 1
    ): String {
        // Security Checks: Prevent weak configuration
        require(iterations >= 1) { "Security Risk: Iterations must be at least 1." }
        require(memoryKb >= 8192) { "Security Risk: Memory must be at least 8 MB (8192 KB) to be effective against GPUs." }
        require(parallelism >= 1) { "Parallelism must be at least 1." }

        // Generate a secure random salt
        val salt = AesEncryptionHelper.Common.generateSecureRandom()

        // Configure Argon2id (Hybrid mode - best against side-channel & GPU attacks)
        val params = Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
            .withVersion(Argon2Parameters.ARGON2_VERSION_13)
            .withIterations(iterations)
            .withMemoryAsKB(memoryKb)
            .withParallelism(parallelism)
            .withSalt(salt)
            .build()

        val generator = Argon2BytesGenerator()
        generator.init(params)

        val hash = ByteArray(DEFAULT_HASH_LENGTH)
        generator.generateBytes(password, hash)

        val saltBase64 = salt.toBase64()
        val hashBase64 = hash.toBase64()

        // Return standard PHC-like formatted string
        val phcString = "\$argon2id\$v=19\$m=$memoryKb,t=$iterations,p=$parallelism\$$saltBase64\$$hashBase64"
        return phcString.toBase64()
    }

    /**
     * Verifies a password against a stored Argon2 hash string.
     * Automatically extracts parameters (salt, memory, iterations) from the hash string.
     * Requires it to be generated with [hash].
     *
     * @return true if the password matches the hash, false otherwise.
     */
    fun verify(password: CharArray, encodedHash: String): Result<Boolean> = runCatching {
        val parts = encodedHash.fromBase64().split("$")
        if (parts.size != 6 || parts[1] != "argon2id") return Result.success(false)

        // Extract parameters
        val paramsMap = parts[3].split(",").associate {
            val p = it.split("=")
            p[0] to p[1].toInt()
        }

        val memoryKb = paramsMap["m"] ?: return Result.success(false)
        val iterations = paramsMap["t"] ?: return Result.success(false)
        val parallelism = paramsMap["p"] ?: return Result.success(false)
        val salt = parts[4].fromBase64ToByteArray()
        val originalHash = parts[5].fromBase64ToByteArray()

        // Re-calculate hash with extracted parameters
        val builder = Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
            .withVersion(Argon2Parameters.ARGON2_VERSION_13)
            .withIterations(iterations)
            .withMemoryAsKB(memoryKb)
            .withParallelism(parallelism)
            .withSalt(salt)
            .build()

        val generator = Argon2BytesGenerator()
        generator.init(builder)

        val newHash = ByteArray(originalHash.size)
        generator.generateBytes(password, newHash)

        MessageDigest.isEqual(newHash, originalHash)
    }
}