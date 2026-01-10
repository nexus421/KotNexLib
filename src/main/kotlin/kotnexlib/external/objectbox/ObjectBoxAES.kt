package kotnexlib.external.objectbox


import io.objectbox.converter.PropertyConverter
import kotnexlib.ExperimentalKotNexLibAPI
import kotnexlib.crypto.AesEncryptionHelper
import kotnexlib.fromBase64ToByteArray
import kotnexlib.toBase64
import javax.crypto.spec.IvParameterSpec

/**
 * Provides functionality for encrypting and decrypting strings using a password-based
 * AES encryption mechanism with optional compression.
 *
 * This object implements `PropertyConverter` for converting strings to and from encrypted
 * representations suitable for storage in a database. It uses a password-based key derivation
 * function (PBKDF2) to derive encryption keys from the specified password.
 * The encryption process involves securely generating a salt and IV for each operation.
 *
 * Note:
 * - The `password` property must be set before performing any encryption or decryption operations.
 * - The `compress` property controls whether the input data is compressed during encryption
 *   or decompressed during decryption.
 * - The implementation assumes safe handling and storage of the encryption-related values
 *   (such as salt and IV) along with the resulting encrypted data.
 *
 * This API is marked as experimental and may be subject to changes in future versions.
 */
@ExperimentalKotNexLibAPI
object CryptoStringEncryptionWithPassword : PropertyConverter<String?, String?> {

    /**
     * The password used for encryption and decryption. Must be set before performing encryption or decryption.
     * Larger is better.
     * Should load password from external source like keystore
     */
    var password: String = ""

    /**
     * Determines whether compression will be applied during encryption or processing operations.
     *
     * When set to `true`, the data will be compressed before being processed or encrypted.
     * If `false`, the data remains uncompressed.
     */
    var compress: Boolean = false

    const val FILE_SEPERATOR = "\u001C"

    override fun convertToEntityProperty(databaseValue: String?): String? {
        if (password.isBlank()) throw IllegalStateException("Password cannot be blank!")
        if (databaseValue.isNullOrBlank()) return null

        //Aus DB laden
        val (salt, iv, encryptedData) = databaseValue.split(FILE_SEPERATOR).let {
            Triple(it[0].fromBase64ToByteArray(), IvParameterSpec(it[1].fromBase64ToByteArray()), it[2])
        }
        val secretKey = AesEncryptionHelper.Common.generateSecureAesKeyFromPassword(password, salt)
        return AesEncryptionHelper.CBC.decrypt(encryptedData, secretKey, iv, compress).getOrThrow()
    }

    override fun convertToDatabaseValue(entityProperty: String?): String? {
        if (password.isBlank()) throw IllegalStateException("Password cannot be blank!")
        if (entityProperty.isNullOrBlank()) return null

        //In DB speichern
        val salt = AesEncryptionHelper.Common.generateSecureRandom(16)
        val secretKey = AesEncryptionHelper.Common.generateSecureAesKeyFromPassword(password, salt)
        val iv = AesEncryptionHelper.Common.getIVSecureRandom().getOrThrow()
        val encryptedData = AesEncryptionHelper.CBC.encrypt(entityProperty, secretKey, iv, compress).getOrThrow()

        return "${salt.toBase64()}$FILE_SEPERATOR${iv.iv.toBase64()}$FILE_SEPERATOR$encryptedData"
    }
}