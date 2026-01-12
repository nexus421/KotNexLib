@file:OptIn(ExperimentalKotNexLibAPI::class)

package kotnexlib.crypto

import kotnexlib.ExperimentalKotNexLibAPI
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AESTest {

    private val testPlaintext = "This is a secret message for testing purposes."
    private val password = "SuperSecretPassword123!"
    private val salt = AES.Common.generateSecureRandom(16)

    @Test
    fun testCommonFunctions() {
        val key = AES.Common.generateAESKey()
        assertNotNull(key)
        assertEquals("AES", key.algorithm)

        val pwKey = AES.Common.generateSecureAesKeyFromPassword(password, salt)
        assertNotNull(pwKey)
        assertEquals("AES", pwKey.algorithm)

        val iv = AES.Common.generateIV().getOrThrow()
        assertNotNull(iv)
        assertEquals(16, iv.iv.size)

        val nonce = AES.Common.generateNonce()
        assertEquals(12, nonce.size)
    }

    @Test
    fun testCBCEncryption() {
        val key = AES.Common.generateAESKey()
        val iv = AES.Common.generateIV().getOrThrow()

        // Direct AES/CBC
        val encryptedResult = AES.CBC.encrypt(testPlaintext, key, iv)
        assertTrue(encryptedResult.isSuccess)
        val encryptedBase64 = encryptedResult.getOrThrow()
        assertNotEquals(testPlaintext, encryptedBase64)

        val decryptedResult = AES.CBC.decrypt(encryptedBase64, key, iv)
        assertTrue(decryptedResult.isSuccess)
        assertEquals(testPlaintext, decryptedResult.getOrThrow())

        // Password Helper (AESData)
        val aesData = AES.CBC.encryptWithPassword(testPlaintext, password)
        assertNotNull(aesData)
        assertEquals(AES.AESType.CBC, aesData!!.type)
        val decryptedPwHelper = aesData.decrypt(password).getOrThrow()
        assertEquals(testPlaintext, decryptedPwHelper)

        // Compression test
        val compressedData = AES.CBC.encryptWithPassword(testPlaintext, password, compress = true)
        assertNotNull(compressedData)
        assertTrue(compressedData!!.compressed)
        assertEquals(testPlaintext, compressedData.decrypt(password).getOrThrow())

        // Legacy Password Methods (ECB)
        val legacyPassword = "1234567812345678" // 16 chars
        val encryptedLegacy = AES.ECB.encryptWithPassword(testPlaintext, legacyPassword)
        assertNotNull(encryptedLegacy)
        val decryptedLegacy =
            AES.ECB.decryptWithPassword(encryptedLegacy.getOrThrow(), legacyPassword)
        assertEquals(testPlaintext, decryptedLegacy.getOrThrow())
    }

    @Test
    fun testGCMEncryption() {
        val key = AES.Common.generateAESKey()
        val nonce = AES.Common.generateNonce()

        // Direct AES/GCM
        val encryptedResult = AES.GCM.encrypt(testPlaintext, key, nonce)
        assertTrue(encryptedResult.isSuccess)
        val encryptedBase64 = encryptedResult.getOrThrow()

        val decryptedResult = AES.GCM.decrypt(encryptedBase64, key, nonce)
        assertTrue(decryptedResult.isSuccess)
        assertEquals(testPlaintext, decryptedResult.getOrThrow())

        // Password Helper (AESData)
        val aesData = AES.GCM.encryptWithPassword(testPlaintext, password)
        assertNotNull(aesData)
        assertEquals(AES.AESType.GCM, aesData.type)
        val decryptedPwHelper = aesData.decrypt(password).getOrThrow()
        assertEquals(testPlaintext, decryptedPwHelper)

        // Compression test
        val compressedData = AES.GCM.encryptWithPassword(testPlaintext, password, compress = true)
        assertTrue(compressedData.compressed)
        assertEquals(testPlaintext, compressedData.decrypt(password).getOrThrow())
    }

    @Test
    fun testAESDataSerialization() {
        // Test CBC Serialization
        val cbcData = AES.CBC.encryptWithPassword(testPlaintext, password)!!
        val cbcString = cbcData.toString()
        val restoredCbcData = AES.AESData.restore(cbcString).getOrThrow()

        assertEquals(cbcData.type, restoredCbcData.type)
        assertEquals(cbcData.encryptedText, restoredCbcData.encryptedText)
        assertEquals(cbcData.iterations, restoredCbcData.iterations)
        assertEquals(cbcData.compressed, restoredCbcData.compressed)
        assertArrayEquals(cbcData.salt, restoredCbcData.salt)
        assertArrayEquals(cbcData.ivOrNonce, restoredCbcData.ivOrNonce)

        assertEquals(testPlaintext, restoredCbcData.decrypt(password).getOrThrow())

        // Test GCM Serialization
        val gcmData = AES.GCM.encryptWithPassword(testPlaintext, password)
        val gcmString = gcmData.toString()
        val restoredGcmData = AES.AESData.restore(gcmString).getOrThrow()

        assertEquals(AES.AESType.GCM, restoredGcmData.type)
        assertEquals(testPlaintext, restoredGcmData.decrypt(password).getOrThrow())
    }

    @Test
    fun testErrorHandling() {
        val key = AES.Common.generateAESKey()
        val iv = AES.Common.generateIV().getOrThrow()
        val nonce = AES.Common.generateNonce()

        // Invalid Base64 for decryption
        val invalidBase64 = "NotBase64!!!"

        val cbcResult = AES.CBC.decrypt(invalidBase64, key, iv)
        assertTrue(cbcResult.isFailure)

        val gcmResult = AES.GCM.decrypt(invalidBase64, key, nonce)
        assertTrue(gcmResult.isFailure)

        // Wrong password length for direct methods
        assertThrows<IllegalArgumentException> {
            AES.ECB.encryptWithPassword(testPlaintext, "short").getOrThrow()
        }
    }
}
