package kotnexlib.external.objectbox

import io.objectbox.converter.PropertyConverter
import kotnexlib.ExperimentalKotNexLibAPI
import kotnexlib.crypto.Argon2Helper
import kotnexlib.crypto.hash

/**
 * A PropertyConverter implementation for hashing strings before saving them to a database.
 *
 * This object facilitates the conversion of data between the entity and database layers by
 * hashing the string values during the database storage process. This is particularly useful
 * for cases where data integrity and security need to be ensured by storing only hashed values.
 *
 * Conversion workflow:
 * - When reading from the database (`convertToEntityProperty`), the data remains unchanged (no decryption or modification).
 * - When writing to the database (`convertToDatabaseValue`), the string values are hashed using a predefined hashing algorithm
 *   to ensure secure and immutable storage.
 *
 *  Use it as an annotation like
 *  @Convert(converter = ObjectBoxHashStringConverter::class, dbType = String::class)
 *  val myHashedField: String
 *
 * Important Notes:
 * - The hashing algorithm used is defined in the `toHash` function, which internally employs a robust hashing mechanism.
 * - The underlying hashing process is based on the `hash` extension function, which defaults to the SHA-256 algorithm.
 * - This class is designed to handle nullable values gracefully.
 */
@ExperimentalKotNexLibAPI
object ObjectBoxHashStringConverter : PropertyConverter<String?, String?> {

    override fun convertToEntityProperty(databaseValue: String?) = databaseValue

    override fun convertToDatabaseValue(entityProperty: String?) = entityProperty?.let { toHash(it) }

    /**
     * Converts the input string to its hashed representation as stored for the database.
     *
     * @param input The string to be hashed.
     * @return The hashed representation of the input string.
     */
    fun toHash(input: String) = input.hash()
}

/**
 * A utility object implementing `PropertyConverter` for hashing strings using the Argon2id algorithm
 * before storing them in a database. This is optimized for usage with ObjectBox.
 *
 * This converter is specifically tailored to handle the conversion of strings:
 * - From the database format to the app's entity property format.
 * - From the app's entity property format to a secure hashed representation optimized for storage.
 *
 * ## Features:
 * - Converts plain text strings to Argon2id hashed strings optimized for mobile devices.
 * - Ensures compatibility with ObjectBox database by implementing the `PropertyConverter` interface.
 *
 * Use it as an annotation like
 * @Convert(converter = ObjectBoxArgon2HashStringConverter::class, dbType = String::class)
 * val myHashedField: String
 *
 * ## Methods:
 * - `convertToEntityProperty`: Returns the database value as is, without any transformation.
 * - `convertToDatabaseValue`: Converts the input string to a secure Argon2id hash.
 * - `toHash`: A helper function that generates a secure hash using `Argon2Helper.hashForMobileDevices`.
 */
@ExperimentalKotNexLibAPI
object ObjectBoxArgon2HashStringConverter : PropertyConverter<String?, String?> {

    override fun convertToEntityProperty(databaseValue: String?) = databaseValue

    override fun convertToDatabaseValue(entityProperty: String?) = entityProperty?.let { toHash(it) }

    /**
     * Converts the input string to its hashed representation as stored for the database.
     *
     * @param input The string to be hashed.
     * @return The hashed representation of the input string.
     */
    fun toHash(input: String) = Argon2Helper.hashForMobileDevices(input.toCharArray())
}

