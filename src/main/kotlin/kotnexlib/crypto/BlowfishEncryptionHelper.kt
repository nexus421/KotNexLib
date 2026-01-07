package kotnexlib.crypto

import kotnexlib.*
import kotnexlib.crypto.BlowfishEncryptionHelper.decryptWithBlowfish
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 * A utility object for performing encryption and decryption using the Blowfish algorithm.
 *
 * Provides methods for encrypting and decrypting strings or byte arrays, with optional GZIP compression.
 * The Blowfish algorithm is considered secure but has limitations due to its 64-bit key size, making it less
 * secure compared to modern encryption standards like AES.
 *
 * Note: Use this implementation with caution, as Blowfish may not be sufficient for high-security needs in certain contexts.
 */
@CriticalAPI("Blowfish is considered insecure and should not be used for new projects.")
object BlowfishEncryptionHelper {

    /**
     * Simple method to encrypt this String with the Blowfish algorithm.
     *
     * Decompress with [decryptWithBlowfish]
     *
     * WARNING: This is not as secure as AES/CBC/PKCS5Padding because the key is only 64 bit long. Whatever: This is still
     * a secure algorithm.
     *
     * @param password will be used for encryption (and decryption!).
     * @param compress if set to true the String will be compressed first and then encrypted. That could minimize the final String size.
     *
     * @return the final encrypted String as Base64 or null on any error.
     */
    fun encryptWithBlowfish(textToEncrypt: String, password: String, compress: Boolean = false): String? {
        return encryptWithBlowfish(textToEncrypt.toByteArray(), password, compress).getOrNull()?.toBase64()
    }

    /**
     * Encrypts the given byte array using the Blowfish algorithm and an input password.
     * Optionally compresses the data using GZIP compression before encryption.
     *
     * @param bytesToEncrypt The byte array to be encrypted.
     * @param password The password used to generate the encryption key.
     * @param compress If `true`, compresses the byte array before encrypting. Default is `false`.
     * @return A `Result` containing the encrypted byte array if successful, or an exception if an error occurs.
     */
    fun encryptWithBlowfish(bytesToEncrypt: ByteArray, password: String, compress: Boolean = false): Result<ByteArray> =
        runCatching {

            val keyData: ByteArray = password.toByteArray()
            val secretKeySpec = SecretKeySpec(keyData, "Blowfish")
            val cipher = Cipher.getInstance("Blowfish").apply {
                init(Cipher.ENCRYPT_MODE, secretKeySpec)
            }

            val encryptedBytes = cipher.doFinal(if (compress) bytesToEncrypt.compress() else bytesToEncrypt)
            return Result.success(encryptedBytes)
        }

    /**
     * Simple method to decrypt this String with the Blowfish algorithm.
     *
     * @param password the password to decrypt the encrypted String.
     * @param isCompressed if set to true, this String will be first decrypted and decompressed afterward. Use this if you encrypted with compression.
     *
     * @return the decrypted String or null on any error.
     */
    fun decryptWithBlowfish(textToDecrypt: String, password: String, isCompressed: Boolean = false): String? {
        return decryptWithBlowfish(textToDecrypt.fromBase64ToByteArray(), password, isCompressed)
            .getOrNull()?.toString(Charsets.UTF_8)
    }

    /**
     * Decrypts the given byte array using the Blowfish algorithm and a specified password.
     * Optionally decompresses the decrypted data if compression was previously applied.
     *
     * @param bytesToDecrypt The byte array to decrypt.
     * @param password The password used as the key for the Blowfish decryption algorithm.
     * @param isCompressed A flag indicating whether the decrypted data should be decompressed. Defaults to `false`.
     * @return A `Result` containing the decrypted byte array, or an exception if decryption fails.
     */
    fun decryptWithBlowfish(
        bytesToDecrypt: ByteArray,
        password: String,
        isCompressed: Boolean = false
    ): Result<ByteArray> = runCatching {
        val keyData: ByteArray = password.toByteArray()
        val secretKeySpec = SecretKeySpec(keyData, "Blowfish")
        val cipher = Cipher.getInstance("Blowfish").apply {
            init(Cipher.DECRYPT_MODE, secretKeySpec)
        }

        val encrypted = cipher.doFinal(bytesToDecrypt)

        return Result.success(if (isCompressed) encrypted.decompress()!! else encrypted)
    }

}