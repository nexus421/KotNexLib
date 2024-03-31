package kotnexlib

import java.security.SecureRandom
import java.security.spec.KeySpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec


/**
 * Generates a new and secure Key for AES to use for encryption.
 */
fun generateAESKey(keySize: Int = 256) = KeyGenerator.getInstance("AES").apply { init(keySize) }.generateKey()

/**
 * Generates a secret key like [generateAESKey] but is based on a [password] with a given [salt].
 * The salt does not need to be kept secret.
 *
 * @param password any password
 * @param salt to make encryption stronger. You may use an 8 byte secure random. Generate it with [generateSecureRandom]
 */
fun generateSecureAesKeyFromPassword(password: String, salt: ByteArray): SecretKeySpec {
    val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
    val spec: KeySpec = PBEKeySpec(password.toCharArray(), salt, 65536, 256)
    val tmp = factory.generateSecret(spec)
    return SecretKeySpec(tmp.encoded, "AES")
}

/**
 * Generates a new and secure Initial Vector (IV) for the given [algorithm]. Defaults to AES.
 */
fun getIVSecureRandom(algorithm: String = "AES"): IvParameterSpec? = tryOrNull({ it.printStackTrace() }) {
    return IvParameterSpec(generateSecureRandom(Cipher.getInstance(algorithm).blockSize))
}

/**
 * Creates a secure random byte array with the given [size]
 */
fun generateSecureRandom(size: Int) = ByteArray(size).apply { SecureRandom.getInstanceStrong().nextBytes(this) }

/**
 * Encrypt this String with a given [secretKey] and a given [ivParameterSpec].
 *
 * [ivParameterSpec] is very important to prevent dictionary attacks.
 * Error will be printed to StdOut.
 *
 * If you have no idea, you may use [encryptWithAesHelper]. This will generate and explain everything to you.
 *
 * @param secretKey Key for encryption
 * @param ivParameterSpec iv for randomness during generation
 * @param compress if set to true, this String will be first compressed and encrypted afterward. Use this for large inputs to reduce size.
 *
 * @return returns the encrypted String as base64 or null on any error.
 */
fun String.encryptWithAES(secretKey: SecretKey, ivParameterSpec: IvParameterSpec, compress: Boolean = false): String? =
    tryOrNull(onError = { it.printStackTrace() }) {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec)
        return@tryOrNull cipher.doFinal((if (compress) compress() else this)!!.toByteArray()).toBase64()
    }

/**
 * Decrypt this String with a given [secretKey] and a given [ivParameterSpec].
 *
 * You need the same [secretKey] and [ivParameterSpec] that were used while encryption!
 * Error will be printed to StdOut.
 *
 * @param secretKey Key for encryption
 * @param ivParameterSpec iv for randomness during generation
 * @param isCompressed if set to true, this String will be first decrypted and decompressed afterward. Use this if you encrypted with compression.
 *
 * @return returns the encrypted String as base64 or null on any error.
 */
fun String.decryptWithAES(
    secretKey: SecretKey,
    ivParameterSpec: IvParameterSpec,
    isCompressed: Boolean = false
): String? = tryOrNull(onError = {
    it.printStackTrace()
}) {
    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec)
    val decrypted = String(cipher.doFinal(fromBase64ToByteArray()), Charsets.UTF_8)

    return@tryOrNull if (isCompressed) decompress() else decrypted
}

/**
 * This method should help you to easily encrypt this String with a random secure key.
 *
 * This String will be encrypted with AES and a 256-Bit strong random key.
 *
 * @param compress if set to true, this String will be first compressed and encrypted afterward. Use this for large inputs to reduce size.
 *
 * @return [AesEncryption]. Read the doc there for more information. The generated secret key can be found there.
 */
fun String.encryptWithAesHelper(compress: Boolean = false): AesEncryption? {
    val iv = getIVSecureRandom() ?: return null
    val key = generateAESKey()
    val encrypted = encryptWithAES(key, iv) ?: return null

    return AesEncryption(encrypted, compress, key, null, iv)
}

/**
 * This method should help you to easily encrypt this String with a password.
 *
 * This String will be encrypted with AES and a 256-Bit strong key.
 *
 * @param password to encrypt the String with. This has to be kept as a secret!
 * @param salt to make the encryption more robust. This has not to be a secret and can be stored globally. By default, this is a random 8 Byte array.
 * You should not use the default implementation here. Generate a salt one time and store it to easily reuse it again.
 * @param compress if set to true, this String will be first compressed and encrypted afterward. Use this for large inputs to reduce size.
 *
 * @return [AesEncryption]. Read the doc there for more information.
 */
fun String.encryptWithAesAndPasswordHelper(
    password: String,
    salt: ByteArray = generateSecureRandom(8),
    compress: Boolean = false
): AesEncryption? {
    val iv = getIVSecureRandom() ?: return null
    val key = generateSecureAesKeyFromPassword(password, salt)
    val encrypted = encryptWithAES(key, iv) ?: return null

    return AesEncryption(encrypted, compress, key, salt, iv)
}

/**
 * Helper class that holds all important information after an encryption with [encryptWithAesHelper].
 *
 * [compressedBeforeEncryption]: This parameter is helpful if you encrypt large Strings. This will decrease the final encrypted result.
 * [key] This is a 256-Bit secure key with secure random numbers. This is the password that was used for encryption and is
 * required for decryption. You will need this key for later decryption!
 * [ivParameterSpec] This is in general not required but highly recommended. So the methods here force you to use one.
 * This will prevent dictionary attacks and will be used while encryption. So save this as this is also required for decryption.
 * [salt] If you used the encryption with a password, you are required to use a salt for better stronger encryption. The salt is not required to
 * keep secret. (but the password is!) So you may store the salt globally.
 *
 * @param encryptedText the encrypted text from [encryptWithAesHelper]
 * @param compressedBeforeEncryption true if the encrypted text was compressed before encryption.
 * @param key secure key that was used to encrypt the String and is required to decrypt.
 * @param salt if you encrypted the text with [encryptWithAesAndPasswordHelper] this field will be filled with the used salt. Otherwise, this is null.
 * @param ivParameterSpec used for randomness to prevent dictionary attacks. Used for encryption and required for decryption.
 */
data class AesEncryption(
    val encryptedText: String,
    val compressedBeforeEncryption: Boolean,
    val key: SecretKey,
    val salt: ByteArray? = null,
    val ivParameterSpec: IvParameterSpec
) {

    /**
     * Directly decrypts [encryptedText] with [decryptWithAES]
     */
    fun decrypt() = encryptedText.decryptWithAES(key, ivParameterSpec)
}

fun main() {
    val result = "Heute ist ein schöner Tag.".encryptWithAesHelper() ?: return
    println(result)
    println(result.encryptedText.decryptWithAES(generateSecureAesKeyFromPassword("Bananarama", ByteArray(8) {
        1.toByte()
    }), result.ivParameterSpec))

}

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
fun String.encryptWithBlowfish(password: String, compress: Boolean = false): String? =
    tryOrNull(onError = {
        it.printStackTrace()
    }) {
        val keyData: ByteArray = password.toByteArray()
        val secretKeySpec = SecretKeySpec(keyData, "Blowfish")
        val cipher = Cipher.getInstance("Blowfish").apply {
            init(Cipher.ENCRYPT_MODE, secretKeySpec)
        }

        val encryptedBytes = cipher.doFinal((if (compress) compress() else this)?.toByteArray())
        return@tryOrNull encryptedBytes.toBase64()
    }

/**
 * Simple method to decrypt this String with the Blowfish algorithm.
 *
 * @param password the password to decrypt the encrypted String.
 * @param isCompressed if set to true, this String will be first decrypted and decompressed afterward. Use this if you encrypted with compression.
 *
 * @return the decrypted String or null on any error.
 */
fun String.decryptWithBlowfish(password: String, isCompressed: Boolean = false): String? =
    tryOrNull(onError = {
        it.printStackTrace()
    }) {
        val keyData: ByteArray = password.toByteArray()
        val secretKeySpec = SecretKeySpec(keyData, "Blowfish")
        val cipher = Cipher.getInstance("Blowfish").apply {
            init(Cipher.DECRYPT_MODE, secretKeySpec)
        }

        val encrypted = String(cipher.doFinal(fromBase64ToByteArray()), Charsets.UTF_8)

        return@tryOrNull if (isCompressed) encrypted.decompress() else encrypted
    }