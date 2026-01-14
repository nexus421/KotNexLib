package kotnexlib.external.objectbox

import io.objectbox.converter.PropertyConverter
import kotnexlib.ExperimentalKotNexLibAPI
import kotnexlib.crypto.Argon2Helper
import kotnexlib.crypto.hash
import kotnexlib.external.objectbox.ObjectBoxArgon2HashStringConverter.convertToDatabaseValue
import kotnexlib.external.objectbox.ObjectBoxArgon2HashStringConverter.convertToEntityProperty
import kotnexlib.external.objectbox.ObjectBoxArgon2HashStringConverter.toHash

/**
 * A PropertyConverter implementation for hashing strings before saving them to a database.
 *
 * This object facilitates the conversion of data between the entity and database layers by
 * hashing the string values during the database storage process. This is particularly useful
 * for cases where data integrity and security need to be ensured by storing only hashed values.
 *
 *  Use it as an annotation like
 *  @Convert(converter = ObjectBoxHashStringConverter::class, dbType = String::class)
 *  val myHashedField: String
 *
 * Important Notes:
 * - The hashing algorithm used is defined in the `toHash` function, which internally employs a robust hashing mechanism.
 * - The underlying hashing process is based on the [hash] extension function, which defaults to the SHA-256 algorithm.
 * - This class is designed to handle nullable values gracefully.
 */
@ExperimentalKotNexLibAPI
object ObjectBoxHashStringConverter : PropertyConverter<String?, String?> {

    override fun convertToEntityProperty(databaseValue: String?) = databaseValue

    override fun convertToDatabaseValue(entityProperty: String?) = entityProperty?.let { toHash(it) }

    /**
     * Converts the input string to its hashed representation as stored for the database.
     *
     * You can use this method also to create a new hash to compare with the stored one!
     *
     * @param input The string to be hashed.
     * @return The hashed representation of the input string.
     */
    fun toHash(input: String) = input.hash()
}

/**
 * A utility object for Objectbox for hashing strings using the Argon2id algorithm before storing them in the database.
 *
 * Converts plain text strings to Argon2id hashed strings optimized for mobile devices. So you don't have to hash
 * the String beforehand.
 *
 * Use it as an annotation like
 * @Convert(converter = ObjectBoxArgon2HashStringConverter::class, dbType = String::class)
 * val myHashedField: String
 *
 * - [convertToEntityProperty]: Returns the hash, previously generated.
 * - [convertToDatabaseValue]: Converts the input string to a secure Argon2id hash.
 * - [toHash]: A helper function that generates a secure hash using [Argon2Helper.hashForMobileDevices].
 */
@ExperimentalKotNexLibAPI
object ObjectBoxArgon2HashStringConverter : PropertyConverter<String?, String?> {

    override fun convertToEntityProperty(databaseValue: String?) = databaseValue

    override fun convertToDatabaseValue(entityProperty: String?) = entityProperty?.let { toHash(it) }

    /**
     * Converts the input string to its hashed representation as stored for the database.
     *
     * You can use this method also to create a new hash to compare with the stored one!
     *
     * @param input The string to be hashed.
     * @return The hashed representation of the input string.
     */
    fun toHash(input: String) = Argon2Helper.hashForMobileDevices(input.toCharArray())
}

