package kotnexlib.crypto

import kotnexlib.ExperimentalKotNexLibAPI
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

/**
 * Authenticated encryption with ChaCha20-Poly1305 (JEP 329, part of the JVM since Java 11).
 * An alternative to [AES.GCM] for platforms without AES-NI hardware acceleration (e.g. many ARM/mobile CPUs).
 *
 * Nonce reuse is catastrophic, exactly as with [AES.GCM]: reusing a nonce with the same key
 * leaks the XOR of the plaintexts and breaks authenticity. Never reuse a (key, nonce) pair.
 */
@ExperimentalKotNexLibAPI
object ChaCha20Poly1305 {

    /** @return a new, random 256-bit key. */
    fun generateKey(): SecretKey = KeyGenerator.getInstance("ChaCha20").generateKey()

    /** @return a new, random 12-byte nonce. Must never be reused with the same key. */
    fun generateNonce(): ByteArray = ByteArray(12).apply { SecureRandom.getInstanceStrong().nextBytes(this) }

    /**
     * @param data the raw data to encrypt.
     * @param secretKey the key, see [generateKey].
     * @param nonce a nonce, see [generateNonce]. Must never be reused with this [secretKey] — the
     * caller is responsible for generating it and storing it alongside the returned ciphertext.
     * @return the encrypted and authenticated data (including the 16-byte Poly1305 tag).
     */
    fun encrypt(data: ByteArray, secretKey: SecretKey, nonce: ByteArray): Result<ByteArray> = runCatching {
        val cipher = Cipher.getInstance("ChaCha20-Poly1305")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(nonce))
        cipher.doFinal(data)
    }

    /**
     * @param encryptedData the result of [encrypt].
     * @param secretKey the same key that was used for encryption.
     * @param nonce the same nonce that was used for encryption.
     * @return the decrypted raw data, or a failed [Result] holding `AEADBadTagException` if the
     * data was tampered with, or the wrong key/nonce was used.
     */
    fun decrypt(encryptedData: ByteArray, secretKey: SecretKey, nonce: ByteArray): Result<ByteArray> = runCatching {
        val cipher = Cipher.getInstance("ChaCha20-Poly1305")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(nonce))
        cipher.doFinal(encryptedData)
    }
}
