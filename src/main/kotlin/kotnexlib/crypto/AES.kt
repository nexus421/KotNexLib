package kotnexlib.crypto

import kotnexlib.*
import java.security.SecureRandom
import java.security.spec.KeySpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Provides a set of utilities and methods for securely handling AES encryption and decryption.
 * This object supports both CBC and GCM modes, offering methods for key generation, IV and nonce creation,
 * and the encryption and decryption of data.
 */
object AES {

    private const val FILE_SEPERATOR = "\u001C"

    enum class AESType {
        CBC, GCM
    }

    /**
     * A utility object providing cryptographic tools for securely managing AES keys, initialization vectors, and nonces.
     * The utilities include methods for generating secure random keys and deriving AES keys from passwords.
     */
    object Common {

        /**
         * Generates a new and secure Key for AES to use for encryption.
         * You should always use the default key size with 256 bit!
         */
        fun generateAESKey(keySize: Int = 256): SecretKey =
            KeyGenerator.getInstance("AES").apply { init(keySize) }.generateKey()

        /**
         * Generates a secure AES key from a given password using the PBKDF2 key derivation function with HMAC SHA-256.
         *
         * The method derives a 256-bit AES encryption key by combining the password with a provided salt
         * and iterating the derivation process a specified number of times to enhance security.
         * A minimum iteration count of 65,536 is enforced to ensure sufficient strength against brute-force attacks.
         *
         * @param password The password used to generate the AES key.
         * @param salt A random byte array used to introduce additional entropy into the key derivation process.
         *             This should be securely stored and reused during decryption.
         * @param iteration The number of iterations for PBKDF2. Defaults to 600,000 if not specified.
         *                  If a value less than 65,536 is provided, 65,536 will be used instead.
         * @return A `SecretKeySpec` containing the derived AES key suitable for encryption or decryption.
         */
        fun generateSecureAesKeyFromPassword(
            password: String,
            salt: ByteArray,
            iteration: Int = 600_000
        ): SecretKeySpec {
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val spec: KeySpec =
                PBEKeySpec(password.toCharArray(), salt, if (iteration < 65536) 65536 else iteration, 256)
            val tmp = factory.generateSecret(spec)
            return SecretKeySpec(tmp.encoded, "AES")
        }

        /**
         * Generates a new and secure Initial Vector (IV) for the given [algorithm]. Defaults to AES.
         */
        fun generateIV(algorithm: String = "AES"): Result<IvParameterSpec> = runCatching {
            IvParameterSpec(generateSecureRandom(Cipher.getInstance(algorithm).blockSize))
        }

        /**
         * Creates a secure random byte array (salt) with the given [size].
         */
        fun generateSecureRandom(size: Int = 16) =
            ByteArray(size).apply { SecureRandom.getInstanceStrong().nextBytes(this) }

        /**
         * Generates a secure nonce for GCM encryption.
         * The recommended size is 12 bytes.
         */
        fun generateNonce(size: Int = 12): ByteArray = generateSecureRandom(size)
    }


    /**
     * Utility object for AES encryption and decryption using the Cipher Block Chaining (CBC) mode with PKCS5 padding.
     *
     * Advantages:
     * - Widely supported and compatible with many legacy systems.
     * - More secure than ECB as it uses an Initialization Vector (IV) to ensure identical blocks of plaintext encrypt to different ciphertext blocks.
     *
     * Disadvantages:
     * - Not inherently authenticated; does not provide integrity checks. Susceptible to padding oracle attacks if not combined with a MAC (like HMAC).
     * - Sequential encryption (cannot be parallelized).
     */
    object CBC {
        /**
         * Encrypt this String with a given [secretKey] and a given [ivParameterSpec] with AES/CBC/PKCS5Padding.
         *
         * [ivParameterSpec] is very important to prevent dictionary attacks.
         *
         * @param text the text to encrypt
         * @param secretKey Key for encryption
         * @param ivParameterSpec iv for randomness during generation
         * @param compress if set to true, this String will be first compressed and encrypted afterward. Use this for large inputs to reduce size.
         *
         * @return returns the encrypted String as base64 as [Result].
         */
        fun encrypt(
            text: String,
            secretKey: SecretKey,
            ivParameterSpec: IvParameterSpec,
            compress: Boolean = false,
        ): Result<String> = runCatching {
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec)
            cipher.doFinal((if (compress) text.compress() else text)!!.toByteArray()).toBase64()
        }

        /**
         * Decrypt this String with a given [secretKey] and a given [ivParameterSpec] with AES/CBC/PKCS5Padding.
         *
         * You need the same [secretKey] and [ivParameterSpec] that were used while encryption!
         *
         * @param encryptedText the encrypted text to decrypt
         * @param secretKey Key for encryption
         * @param ivParameterSpec iv for randomness during generation
         * @param isCompressed if set to true, this String will be first decrypted and decompressed afterward. Use this if you encrypted with compression.
         *
         * @return returns the decrypted String as [Result].
         */
        fun decrypt(
            encryptedText: String,
            secretKey: SecretKey,
            ivParameterSpec: IvParameterSpec,
            isCompressed: Boolean = false,
        ): Result<String> = runCatching {
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec)
            val decrypted = String(cipher.doFinal(encryptedText.fromBase64ToByteArray()), Charsets.UTF_8)

            if (isCompressed) decrypted.decompress()!! else decrypted
        }

        /**
         * Decrypt this ByteArray with a given [secretKey] and a given [ivParameterSpec] with AES/CBC/PKCS5Padding.
         *
         * You need the same [secretKey] and [ivParameterSpec] that were used while encryption!
         *
         * @param encryptedText the encrypted text to decrypt
         * @param secretKey Key for encryption
         * @param ivParameterSpec iv for randomness during generation
         * @param isCompressed if set to true, this String will be first decrypted and decompressed afterward. Use this if you encrypted with compression.
         *
         * @return returns the decrypted String as [Result].
         */
        fun decryptToByteArray(
            encryptedText: String,
            secretKey: SecretKey,
            ivParameterSpec: IvParameterSpec,
            isCompressed: Boolean = false,
        ): Result<ByteArray> = runCatching {
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec)
            val decrypted = cipher.doFinal(encryptedText.fromBase64ToByteArray())

            if (isCompressed) decrypted.decompress()!! else decrypted
        }

        /**
         * This method should help you to easily encrypt this String with a password.
         *
         * This String will be encrypted with AES and a 256-Bit strong key with AES/CBC/PKCS5Padding.
         * Most will be randomly generated. Check the result for all used informations used for encryption.
         *
         * @param text the text to encrypt
         * @param password to encrypt the String with. This has to be kept as a secret!
         * @param salt to make the encryption more robust. This has not to be a secret and can be stored globally. By default, this is a random 16 Byte array.
         * You can reuse the salt or generate a new one any time (recommended).
         * @param compress if set to true, this String will be first compressed and encrypted afterward. Use this for large inputs to reduce size.
         *
         * @return [AESData]. Read the doc there for more information.
         */
        fun encryptWithPassword(
            text: String,
            password: String,
            salt: ByteArray = Common.generateSecureRandom(16),
            compress: Boolean = false
        ): AESData? {
            val iv = Common.generateIV().getOrNull() ?: return null
            val iterations = 600_000
            val key = Common.generateSecureAesKeyFromPassword(password, salt, iterations)
            val encrypted = encrypt(text, key, iv, compress).getOrNull() ?: return null

            return AESData(encrypted, compress, salt, iterations, iv.iv, AESType.CBC)
        }

    }

    /**
     * A data class representing encrypted AES data and its associated metadata.
     *
     * @property encryptedText The encrypted text as a Base64-encoded string.
     * @property compressed A boolean indicating whether the original data was compressed before encryption.
     * @property salt The salt used during key generation for the AES encryption process.
     * @property iterations The number of iterations used in the key derivation process.
     * @property ivOrNonce The initialization vector (IV) or nonce used for encryption.
     * @property type The type of AES encryption used (CBC or GCM).
     */
    class AESData internal constructor(
        val encryptedText: String,
        val compressed: Boolean,
        val salt: ByteArray,
        val iterations: Int,
        val ivOrNonce: ByteArray,
        val type: AESType
    ) {

        companion object {

            /**
             * Restores an AESData object from its Base64-encoded [AES.AESData.toString] representation.
             *
             * The input string must contain serialized fields of the AESData object, separated by a predefined file separator.
             *
             * @param data A Base64-encoded string containing the serialized fields of an AESData object.
             * @return A Result object containing the restored AESData instance or an exception if restoration fails.
             */
            fun restore(data: String): Result<AESData> = runCatching {
                val parts = data.fromBase64().split(FILE_SEPERATOR)
                AESData(
                    type = AESType.valueOf(parts[0]),
                    ivOrNonce = parts[1].fromBase64ToByteArray(),
                    iterations = parts[2].toInt(),
                    salt = parts[3].fromBase64ToByteArray(),
                    compressed = parts[4].toBoolean(),
                    encryptedText = parts[5]
                )
            }
        }

        /**
         * Converts the AESData object into its Base64-encoded string representation.
         *
         * The string representation includes the following serialized fields in order:
         * - The type name of the AESData object.
         * - A file separator.
         * - The Base64-encoded initialization vector (IV) or nonce.
         * - A file separator.
         * - The iterations count of the cryptographic operation.
         * - A file separator.
         * - The Base64-encoded salt.
         * - A file separator.
         * - A flag indicating whether the data is compressed.
         * - A file separator.
         * - The encrypted text.
         *
         * The entire concatenated string is encoded into a single Base64 string.
         *
         * Through this string representation, the AESData object can be restored from its serialized form very easy.
         * This is safe to store (e.g. in a database).
         *
         * @return A Base64-encoded string representation of the AESData object.
         */
        override fun toString() = buildString {
            append(type.name)
            append(FILE_SEPERATOR)
            append(ivOrNonce.toBase64())
            append(FILE_SEPERATOR)
            append(iterations)
            append(FILE_SEPERATOR)
            append(salt.toBase64())
            append(FILE_SEPERATOR)
            append(compressed)
            append(FILE_SEPERATOR)
            append(encryptedText)
        }.toBase64()

        /**
         * Decrypts the encrypted text within the AESData object using the specified decryption type (e.g., AES/CBC or AES/GCM)
         * and a derived encryption key from the provided password.
         *
         * Note: This only works for text-based data. Other datas could lose information during decryption.
         *
         * The decryption process involves deriving a secure AES key from the given password, salt, and iteration count,
         * and using the corresponding AES decryption algorithm with the stored initialization vector (IV) or nonce.
         * If the data was compressed during encryption, decompression will be applied during decryption.
         *
         * @param password The password used to derive the encryption key for decryption.
         */
        fun decryptAsString(password: String) = when (type) {
            AESType.CBC -> CBC.decrypt(
                encryptedText,
                Common.generateSecureAesKeyFromPassword(password, salt, iterations),
                IvParameterSpec(ivOrNonce),
                compressed
            )

            AESType.GCM -> GCM.decrypt(
                encryptedText,
                Common.generateSecureAesKeyFromPassword(password, salt, iterations),
                ivOrNonce,
                compressed
            )
        }

        /**
         * Decrypts the encrypted data stored in the AESData object into a byte array using the specified decryption type
         * (AES/CBC or AES/GCM) and a derived encryption key from the provided password.
         *
         * The process involves using the AES decryption algorithm (CBC or GCM), deriving a secure AES key based on the
         * given password, salt, and iteration count, and applying the relevant initialization vector (IV) or nonce. If
         * the data was compressed during encryption, decompression is also applied after the decryption step.
         *
         * @param password The password used to derive the encryption key for decryption.
         * @return A Result<ByteArray> containing the decrypted byte array or an error if decryption fails.
         */
        fun decryptAsByteArray(password: String) = when (type) {
            AESType.CBC -> CBC.decryptToByteArray(
                encryptedText,
                Common.generateSecureAesKeyFromPassword(password, salt, iterations),
                IvParameterSpec(ivOrNonce),
                compressed
            )

            AESType.GCM -> GCM.decryptToByteArray(
                encryptedText,
                Common.generateSecureAesKeyFromPassword(password, salt, iterations),
                ivOrNonce,
                compressed
            )
        }

    }

    /**
     * A utility object for performing encryption and decryption using the AES algorithm
     * in ECB mode with PKCS5 padding.
     *
     * Provides methods to encrypt and decrypt strings with a password. Optional GZIP
     * compression is available to reduce data size before encryption.
     *
     * Note: AES in ECB mode is less secure compared to other modes like CBC or GCM
     * due to its deterministic nature for identical plaintext blocks. Users are
     * encouraged to prefer more secure cryptographic approaches for sensitive data.
     */
    object ECB {

        /**
         * Encrypts this String with AES/ECB/PKCS5Padding with the given [password].
         * Note: This is not as secure as [CBC.encrypt] but simpler. Dictionary attacks are possible.
         *
         * @param text the text to encrypt
         * @param password the password this String will be encrypted with. Length must be 16 or 32!
         * @param compress if set to true this String will be compressed first and encrypted afterward.
         *
         * @return the encrypted string as Base64 as [Result].
         */
        fun encryptWithPassword(
            text: String,
            password: String,
            compress: Boolean = false
        ) = runCatching {
            if (password.length != 16 && password.length != 32) throw IllegalArgumentException("The password must be of a length of 16 or 32 characters!")
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding").apply {
                init(Cipher.ENCRYPT_MODE, SecretKeySpec(password.toByteArray(), "AES"))
            }
            cipher.doFinal((if (compress) text.compress() else text)!!.toByteArray()).toBase64()
        }

        /**
         * Decrypts this String with the given [password] which was encrypted with AES/ECB/PKCS5Padding.
         *
         * @param encryptedText the encrypted text to decrypt
         * @param password to decrypt this String
         * @param isCompressed if set to true this String will be decompressed afterward.
         *
         * @return the decrypted string as [Result].
         */
        fun decryptWithPassword(
            encryptedText: String,
            password: String,
            isCompressed: Boolean = false
        ) = runCatching {
            if (password.length != 16 && password.length != 32) throw IllegalStateException("The password must be of a length of 16 or 32 characters!")
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding").apply {
                init(Cipher.DECRYPT_MODE, SecretKeySpec(password.toByteArray(), "AES"))
            }
            val decrypted = String(cipher.doFinal(encryptedText.fromBase64ToByteArray()), Charsets.UTF_8)

            if (isCompressed) decrypted.decompress() else decrypted
        }
    }

    /**
     * Utility object for AES encryption and decryption using the Galois/Counter Mode (GCM) with NoPadding.
     *
     * Advantages:
     * - Authenticated Encryption: Provides both confidentiality and data integrity (detects unauthorized modifications).
     * - Parallelizable: Highly efficient as encryption can be computed in parallel.
     * - No padding needed.
     *
     * Disadvantages:
     * - Nonce reuse is catastrophic: Reusing a nonce with the same key breaks the security completely.
     */
    @ExperimentalKotNexLibAPI
    object GCM {
        /**
         * Encrypt this String with a given [secretKey] and a given [nonce] with AES/GCM/NoPadding.
         *
         * [nonce] is very important to prevent dictionary attacks. Recommended size is 12 bytes.
         *
         * @param text the text to encrypt
         * @param secretKey Key for encryption
         * @param nonce nonce for randomness during generation (recommended 12 bytes)
         * @param compress if set to true, this String will be first compressed and encrypted afterward. Use this for large inputs to reduce size.
         *
         * @return returns the encrypted String as base64 as [Result].
         */
        fun encrypt(
            text: String,
            secretKey: SecretKey,
            nonce: ByteArray = Common.generateNonce(),
            compress: Boolean = false,
        ): Result<String> = encrypt(text.toByteArray(), secretKey, nonce, compress)

        /**
         * Encrypt this ByteArray with a given [secretKey] and a given [nonce] with AES/GCM/NoPadding.
         *
         * [nonce] is very important to prevent dictionary attacks. Recommended size is 12 bytes.
         *
         * @param data to encrypt
         * @param secretKey Key for encryption
         * @param nonce nonce for randomness during generation (recommended 12 bytes)
         * @param compress if set to true, this String will be first compressed and encrypted afterward. Use this for large inputs to reduce size.
         *
         * @return returns the encrypted String as base64 as [Result].
         */
        fun encrypt(
            data: ByteArray,
            secretKey: SecretKey,
            nonce: ByteArray = Common.generateNonce(),
            compress: Boolean = false,
        ): Result<String> = runCatching {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(128, nonce)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)
            cipher.doFinal((if (compress) data.compress() else data)).toBase64()
        }

        /**
         * Decrypt this String with a given [secretKey] and a given [nonce] with AES/GCM/NoPadding.
         *
         * You need the same [secretKey] and [nonce] that were used while encryption!
         *
         * @param encryptedText the encrypted text to decrypt
         * @param secretKey Key for encryption
         * @param nonce nonce for randomness during generation
         * @param isCompressed if set to true, this String will be first decrypted and decompressed afterward. Use this if you encrypted with compression.
         *
         * @return returns the decrypted String as [Result].
         */
        fun decrypt(
            encryptedText: String,
            secretKey: SecretKey,
            nonce: ByteArray,
            isCompressed: Boolean = false
        ): Result<String> = runCatching {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(128, nonce)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            val decrypted = String(cipher.doFinal(encryptedText.fromBase64ToByteArray()), Charsets.UTF_8)

            if (isCompressed) decrypted.decompress()!! else decrypted
        }

        fun decryptToByteArray(
            encryptedText: String,
            secretKey: SecretKey,
            nonce: ByteArray,
            isCompressed: Boolean = false
        ): Result<ByteArray> = runCatching {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(128, nonce)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            val decrypted = cipher.doFinal(encryptedText.fromBase64ToByteArray())

            if (isCompressed) decrypted.decompress()!! else decrypted
        }

        /**
         * This method should help you to easily encrypt this String with a password.
         *
         * This String will be encrypted with AES and a 256-Bit strong key with AES/GCM/NoPadding.
         * Most will be randomly generated. Check the result for all used informations used for encryption.
         *
         * @param text the text to encrypt
         * @param password to encrypt the String with. This has to be kept as a secret!
         * @param salt to make the encryption more robust. This has not to be a secret and can be stored globally. By default, this is a random 16 Byte array.
         * You can reuse the salt or generate a new one any time (recommended).
         * @param compress if set to true, this String will be first compressed and encrypted afterward. Use this for large inputs to reduce size.
         *
         * @return [AESData]. Read the doc there for more information.
         */
        fun encryptWithPassword(
            text: String,
            password: String,
            salt: ByteArray = Common.generateSecureRandom(16),
            compress: Boolean = false
        ): AESData = encryptWithPassword(text.toByteArray(), password, salt, compress)

        /**
         * This method should help you to easily encrypt this ByteArray with a password.
         *
         * This String will be encrypted with AES and a 256-Bit strong key with AES/GCM/NoPadding.
         * Most will be randomly generated. Check the result for all used informations used for encryption.
         *
         * @param data to encrypt
         * @param password to encrypt the String with. This has to be kept as a secret!
         * @param salt to make the encryption more robust. This has not to be a secret and can be stored globally. By default, this is a random 16 Byte array.
         * You can reuse the salt or generate a new one any time (recommended).
         * @param compress if set to true, this String will be first compressed and encrypted afterward. Use this for large inputs to reduce size.
         *
         * @return [AESData]. Read the doc there for more information.
         */
        fun encryptWithPassword(
            data: ByteArray,
            password: String,
            salt: ByteArray = Common.generateSecureRandom(16),
            compress: Boolean = false
        ): AESData {
            val nonce = Common.generateNonce()
            val iterations = 600_000
            val key = Common.generateSecureAesKeyFromPassword(password, salt, iterations)
            val encrypted = encrypt(data, key, nonce, compress).getOrThrow()

            return AESData(encrypted, compress, salt, iterations, nonce, AESType.GCM)
        }


    }
}