@file:OptIn(ExperimentalKotNexLibAPI::class)

package kotnexlib.crypto

import kotnexlib.ExperimentalKotNexLibAPI
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ChaCha20Poly1305Test {

    @Test
    fun testRoundtripForVariousSizes() {
        val key = ChaCha20Poly1305.generateKey()
        for (size in listOf(0, 1, 16, 33, 1024)) {
            val nonce = ChaCha20Poly1305.generateNonce()
            val data = ByteArray(size) { it.toByte() }

            val encrypted = ChaCha20Poly1305.encrypt(data, key, nonce).getOrThrow()
            assertEquals(size + 16, encrypted.size) // ciphertext + 16-byte Poly1305 tag

            val decrypted = ChaCha20Poly1305.decrypt(encrypted, key, nonce).getOrThrow()
            assertArrayEquals(data, decrypted)
        }
    }

    @Test
    fun testWrongKeyFailsDecryption() {
        val nonce = ChaCha20Poly1305.generateNonce()
        val ciphertext =
            ChaCha20Poly1305.encrypt("secret".toByteArray(), ChaCha20Poly1305.generateKey(), nonce).getOrThrow()

        val result = ChaCha20Poly1305.decrypt(ciphertext, ChaCha20Poly1305.generateKey(), nonce)
        assertTrue(result.isFailure)
    }

    @Test
    fun testWrongNonceFailsDecryption() {
        val key = ChaCha20Poly1305.generateKey()
        val ciphertext =
            ChaCha20Poly1305.encrypt("secret".toByteArray(), key, ChaCha20Poly1305.generateNonce()).getOrThrow()

        val result = ChaCha20Poly1305.decrypt(ciphertext, key, ChaCha20Poly1305.generateNonce())
        assertTrue(result.isFailure)
    }

    @Test
    fun testTamperedCiphertextFailsDecryption() {
        val key = ChaCha20Poly1305.generateKey()
        val nonce = ChaCha20Poly1305.generateNonce()
        val ciphertext = ChaCha20Poly1305.encrypt("authenticated data".toByteArray(), key, nonce).getOrThrow()
        ciphertext[0] = ciphertext[0].inc()

        assertTrue(ChaCha20Poly1305.decrypt(ciphertext, key, nonce).isFailure)
    }

    @Test
    fun testNonceReuseLeaksPlaintextXor() {
        // Demonstrates why a (key, nonce) pair must never be reused: with a stream cipher, reusing the
        // keystream lets an attacker recover XOR(plaintext1, plaintext2) from the two ciphertexts alone.
        val key = ChaCha20Poly1305.generateKey()
        val nonce = ChaCha20Poly1305.generateNonce()
        val plaintext1 = "AAAAAAAAAAAAAAAA".toByteArray()
        val plaintext2 = "BBBBBBBBBBBBBBBB".toByteArray()

        val cipher1 = ChaCha20Poly1305.encrypt(plaintext1, key, nonce).getOrThrow()
        val cipher2 = ChaCha20Poly1305.encrypt(plaintext2, key, nonce).getOrThrow()

        val xorCiphertexts = ByteArray(plaintext1.size) { (cipher1[it].toInt() xor cipher2[it].toInt()).toByte() }
        val xorPlaintexts = ByteArray(plaintext1.size) { (plaintext1[it].toInt() xor plaintext2[it].toInt()).toByte() }
        assertArrayEquals(xorPlaintexts, xorCiphertexts)
    }
}
