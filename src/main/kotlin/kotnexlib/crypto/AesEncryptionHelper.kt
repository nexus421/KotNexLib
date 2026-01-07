package kotnexlib.crypto

import kotnexlib.*
import kotnexlib.crypto.AesEncryptionHelper.CBC.decryptWithAES
import kotnexlib.crypto.AesEncryptionHelper.CBC.encryptWithAES
import kotnexlib.crypto.AesEncryptionHelper.CBC.encryptWithAesAndPasswordHelper
import kotnexlib.crypto.AesEncryptionHelper.CBC.encryptWithAesHelper
import java.security.SecureRandom
import java.security.spec.KeySpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object AesEncryptionHelper {

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
        fun getIVSecureRandom(algorithm: String = "AES"): Result<IvParameterSpec> = runCatching {
            return Result.success(IvParameterSpec(generateSecureRandom(Cipher.getInstance(algorithm).blockSize)))
        }

        /**
         * Creates a secure random byte array with the given [size]
         */
        fun generateSecureRandom(size: Int = 16) =
            ByteArray(size).apply { SecureRandom.getInstanceStrong().nextBytes(this) }
    }


    /**
     * Utility object for AES encryption and decryption using the CBC mode with PKCS5 padding.
     * This provides helper methods for encryption and decryption with additional tools to use passwords and random key generation.
     */
    object CBC {
        /**
         * Encrypt this String with a given [secretKey] and a given [ivParameterSpec] with AES/CBC/PKCS5Padding.
         *
         * [ivParameterSpec] is very important to prevent dictionary attacks.
         * Error will be printed to StdOut.
         *
         * If you have no idea, you may use [encryptWithAesHelper]. This will generate and explain everything to you.
         *
         * @param text the text to encrypt
         * @param secretKey Key for encryption
         * @param ivParameterSpec iv for randomness during generation
         * @param compress if set to true, this String will be first compressed and encrypted afterward. Use this for large inputs to reduce size.
         *
         * @return returns the encrypted String as base64 or null on any error.
         */
        fun encryptWithAES(
            text: String,
            secretKey: SecretKey,
            ivParameterSpec: IvParameterSpec,
            compress: Boolean = false,
        ): Result<String> = runCatching {
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec)
            return Result.success(cipher.doFinal((if (compress) text.compress() else text)!!.toByteArray()).toBase64())
        }

        /**
         * Decrypt this String with a given [secretKey] and a given [ivParameterSpec] with AES/CBC/PKCS5Padding.
         *
         * You need the same [secretKey] and [ivParameterSpec] that were used while encryption!
         * Error will be printed to StdOut.
         *
         * @param encryptedText the encrypted text to decrypt
         * @param secretKey Key for encryption
         * @param ivParameterSpec iv for randomness during generation
         * @param isCompressed if set to true, this String will be first decrypted and decompressed afterward. Use this if you encrypted with compression.
         * @param onError will be called on any thrown exception. Default: Prints to stacktrace
         *
         * @return returns the encrypted String as base64 or null on any error.
         */
        fun decryptWithAES(
            encryptedText: String,
            secretKey: SecretKey,
            ivParameterSpec: IvParameterSpec,
            isCompressed: Boolean = false,
            onError: (Throwable) -> Unit = { it.printStackTrace() }
        ): Result<String> = runCatching {
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec)
            val decrypted = String(cipher.doFinal(encryptedText.fromBase64ToByteArray()), Charsets.UTF_8)

            return Result.success(if (isCompressed) decrypted.decompress()!! else decrypted)
        }

        /**
         * This method should help you to easily encrypt this String with a random secure key.
         *
         * This String will be encrypted with AES and a 256-Bit strong random key with AES/CBC/PKCS5Padding.
         * Everything will be randomly generated. Check the result for all used informations used for encryption.
         *
         * @param text the text to encrypt
         * @param compress if set to true, this String will be first compressed and encrypted afterward. Use this for large inputs to reduce size.
         *
         * @return [AesEncryption]. Read the doc there for more information. The generated secret key can be found there.
         */
        fun encryptWithAesHelper(text: String, compress: Boolean = false): AesEncryption? {
            val iv = Common.getIVSecureRandom().getOrNull() ?: return null
            val key = Common.generateAESKey()
            val encrypted = encryptWithAES(text, key, iv, compress)

            return AesEncryption(encrypted.getOrThrow(), compress, key, null, iv)
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
         * You can reuse the salt or generate a new one any time (recomended).
         * @param compress if set to true, this String will be first compressed and encrypted afterward. Use this for large inputs to reduce size.
         * @param onError will be called on any thrown exception. Default: Prints to stacktrace
         *
         * @return [AesEncryption]. Read the doc there for more information.
         */
        fun encryptWithAesAndPasswordHelper(
            text: String,
            password: String,
            salt: ByteArray = Common.generateSecureRandom(16),
            compress: Boolean = false,
            onError: (Throwable) -> Unit = { it.printStackTrace() }
        ): AesEncryption? {
            val iv = Common.getIVSecureRandom().getOrNull() ?: return null
            val key = Common.generateSecureAesKeyFromPassword(password, salt)
            val encrypted = encryptWithAES(text, key, iv, compress).getOrNull() ?: return null

            return AesEncryption(encrypted, compress, key, salt, iv)
        }

        /**
         * Helper class that holds all important information after an encryption with [encryptWithAesHelper].
         *
         * [compressedBeforeEncryption]: true, if the result was compressed before encryption
         * [key] This is a 256-Bit (only in default) secure key with secure random numbers. This is the password that was used for encryption and is
         * required for decryption. You will need this key for later decryption!
         * [ivParameterSpec] This is in general not required but highly recommended. So the methods here force you to use one.
         * This will prevent dictionary attacks and will be used while encryption. So save this as this is also required for decryption. The iv can be stored within a database. No security needed.
         * [salt] If you used the encryption with a password, you are required to use a salt for better stronger encryption. The salt is not required to
         * keep secret. (but the password is!) So you may store the salt globally or create a new one every time you encrypt.
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
            fun decrypt() = decryptWithAES(encryptedText, key, ivParameterSpec)

            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as AesEncryption

                if (compressedBeforeEncryption != other.compressedBeforeEncryption) return false
                if (encryptedText != other.encryptedText) return false
                if (key != other.key) return false
                if (!salt.contentEquals(other.salt)) return false
                if (ivParameterSpec != other.ivParameterSpec) return false

                return true
            }

            override fun hashCode(): Int {
                var result = compressedBeforeEncryption.hashCode()
                result = 31 * result + encryptedText.hashCode()
                result = 31 * result + key.hashCode()
                result = 31 * result + (salt?.contentHashCode() ?: 0)
                result = 31 * result + ivParameterSpec.hashCode()
                return result
            }
        }

        /**
         * Encrypts this String with AES/ECB/PKCS5Padding with the given [password].
         * Note: This is not as secure as [encryptWithAES] but simpler. Dictionary attacks are possible.
         *
         * @param text the text to encrypt
         * @param password the password this String will be encrypted. Length must be 16 or 32!
         * @param compress if set to true this String will be compressed first and encrypted afterward.
         * @param onError will be called on any thrown exception. Default: Prints to stacktrace
         *
         * @return the encrypted string as Base64 or null on any error.
         */
        fun encryptWithAesAndPassword(
            text: String,
            password: String,
            compress: Boolean = false,
            onError: (Throwable) -> Unit = { it.printStackTrace() }
        ) = tryOrNull(onError = onError) {
            if (password.length != 16 && password.length != 32) throw IllegalStateException("The password must be of a length of 16 or 32 characters!")
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
         * @param isCompressed if set to true this String will be compressed first and encrypted afterward.
         * @param onError will be called on any thrown exception. Default: Prints to stacktrace
         *
         * @return the decrypted string or null on any error.
         */
        fun decryptWithAesAndPassword(
            encryptedText: String,
            password: String,
            isCompressed: Boolean = false,
            onError: (Throwable) -> Unit = { it.printStackTrace() }
        ) = tryOrNull(onError = onError) {
            if (password.length != 16 && password.length != 32) throw IllegalStateException("The password must be of a length of 16 or 32 characters!")
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding").apply {
                init(Cipher.DECRYPT_MODE, SecretKeySpec(password.toByteArray(), "AES"))
            }
            val decrypted = String(cipher.doFinal(encryptedText.fromBase64ToByteArray()), Charsets.UTF_8)

            if (isCompressed) decrypted.decompress() else decrypted
        }
    }



}