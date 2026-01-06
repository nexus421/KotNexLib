package kotnexlib.external.objectbox

import io.objectbox.converter.PropertyConverter
import kotnexlib.ExperimentalKotNexLibAPI
import kotnexlib.crypto.BlowfishEncryptionHelper
import kotnexlib.getRandomString

/**
 * Provides functionality to convert a `BFString` object to a database-storable encrypted `String`
 * and vice versa. This utility is primarily designed for use with ObjectBox and supports seamless
 * data transformation by encrypting the string data during storage and decrypting it when retrieved.
 *
 * The class leverages the Blowfish encryption algorithm through the `BFString` type, ensuring data
 * is securely stored with optional compression and metadata for decryption.
 *
 * This class is marked as experimental, and its API might change in future versions. Use it carefully
 * in production or stable environments.
 *
 * Implements:
 * - `convertToEntityProperty(String?)`: Converts a raw database string into a `BFString` instance by parsing
 *   its components, such as salt and compression flag, and marking it as encrypted.
 * - `convertToDatabaseValue(BFString?)`: Converts a `BFString` instance into a formatted and encrypted string
 *   that is safe for database storage.
 */
@ExperimentalKotNexLibAPI
object BlowfishStringEncryption : PropertyConverter<BFString?, String?> {

    override fun convertToEntityProperty(databaseValue: String?): BFString? {
        if (databaseValue.isNullOrBlank()) return null
        return BFString.fromRawString(databaseValue)
    }

    override fun convertToDatabaseValue(entityProperty: BFString?): String? {
        return entityProperty?.encryptToDbString()
    }
}

/**
 * A utility object for encrypting and decrypting nullable strings using the Blowfish encryption algorithm.
 * This object is primarily designed for integration with ObjectBox as a `PropertyConverter`.
 *
 * It utilizes the `BFString` class to handle the encryption and decryption process, which includes additional
 * features like optional string compression and secure handling of salts. The password used for encryption
 * and decryption must be explicitly provided or set in the `password` property.
 *
 * This class is marked as experimental and may undergo changes in the future. Users must opt-in using the
 * `ExperimentalKotNexLibAPI` annotation.
 *
 * Features:
 * - Encrypts strings with Blowfish and stores them in a database-friendly format.
 * - Decrypts database-stored strings back to their original form.
 * - Supports optional compression of strings during encryption.
 *
 * To use this converter:
 * - Set the `password` property before performing encryption or decryption.
 * - Optionally enable or disable `compress` to control compression during encryption.
 *
 * Note:
 * - Decryption will fail if the `password` provided is incorrect.
 * - The encryption and decryption logic rely on properly formatted input/output based on the `BFString` class.
 *
 * Implements:
 * - `PropertyConverter<String?, String?>`: Converts between entity property strings and database-compatible strings.
 */
@ExperimentalKotNexLibAPI
object BlowfishStringEncryptionWithPassword : PropertyConverter<String?, String?> {

    /**
     * The password used for encryption and decryption. Must be set before performing encryption or decryption.
     * Larger is better.
     */
    var password: String = ""

    /**
     * Determines whether compression will be applied during encryption or processing operations.
     *
     * When set to `true`, the data will be compressed before being processed or encrypted.
     * If `false`, the data remains uncompressed.
     */
    var compress: Boolean = false

    override fun convertToEntityProperty(databaseValue: String?): String? {
        if (password.isBlank()) throw IllegalStateException("Password cannot be blank!")
        if (databaseValue.isNullOrBlank()) return null
        return BFString.fromRawString(databaseValue).decrypt(password)
    }

    override fun convertToDatabaseValue(entityProperty: String?): String? {
        if (password.isBlank()) throw IllegalStateException("Password cannot be blank!")
        if (entityProperty.isNullOrBlank()) return null
        return BFString(entityProperty, password, compress = compress).encryptToDbString()
    }
}

/**
 * Represents a string that can be encrypted and decrypted with the Blowfish encryption algorithm, mainly for ObjectBox.
 * The class supports optional compression of the string during encryption and stores metadata like salt
 * and compression flags for proper decryption.
 *
 * If stored within the DB, the password will be wiped. After loading from DB you need to enter the password again.
 * You may create a [copy] with the required password to use the [decrypt] method.
 *
 * @property text The raw string that will be encrypted or decrypted.
 * @property password The password used for encryption and decryption.
 * @property salt A random string appended to the password for added security during encryption.
 * @property compress A flag specifying whether the string should be compressed during encryption.
 * @property isEncrypted Indicates whether the current instance of the class is in an encrypted state.
 */
data class BFString(
    private val text: String,
    private val password: String,
    private val salt: String = getRandomString(8),
    private val compress: Boolean = false
) {

    var isEncrypted = false
        private set

    companion object {
        const val FILE_SEPERATOR = "\u001C"
        fun fromRawString(rawText: String): BFString {
            return rawText
                .split(FILE_SEPERATOR)
                .let {
                    BFString(it[2], "", it[0], it[1].toBoolean())
                        .apply { isEncrypted = true }
                }
        }
    }

    /**
     * Encrypts the current text of the BFString instance using the Blowfish algorithm.
     * Combines the provided password with the instance's generated salt and optionally applies compression
     * based on the compress flag. The result includes the salt, compression flag, and the encrypted text,
     * separated by a defined delimiter.
     *
     * @return A formatted string containing the salt, compression flag, and the encrypted text, or null if encryption fails.
     */
    fun encryptToDbString(): String? {
        if (password.isBlank()) throw IllegalStateException("Password cannot be blank! Password will not be stored in DB and will be empty after loading from DB!")
        val encryptedString =
            BlowfishEncryptionHelper.encryptWithBlowfish(text, password + salt, compress) ?: return null
        return "$salt$FILE_SEPERATOR$compress$FILE_SEPERATOR$encryptedString"
    }

    /**
     * Decrypts the current BFString instance's text using the Blowfish algorithm.
     * Combines the provided password with the instance's salt and optionally applies decompression
     * if the instance's compress flag is set to true.
     *
     * @param password The password to be combined with the salt for decryption.
     * @return The decrypted string, or null if decryption fails.
     */
    fun decrypt(password: String): String? {
        if (password.isBlank()) throw IllegalStateException("Password cannot be blank!")
        if (isEncrypted.not()) {
            println("Object is not encrypted!")
            return text
        }
        return BlowfishEncryptionHelper.decryptWithBlowfish(text, password + salt, compress)
    }
}