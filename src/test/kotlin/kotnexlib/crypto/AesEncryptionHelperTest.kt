@file:OptIn(ExperimentalKotNexLibAPI::class)

package kotnexlib.crypto

import kotnexlib.ExperimentalKotNexLibAPI
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AesEncryptionHelperTest {

    private val testPlaintext = "This is a secret message for testing purposes."
    private val password = "SuperSecretPassword123!"
    private val salt = AesEncryptionHelper.Common.generateSecureRandom(16)

    @Test
    fun testCommonFunctions() {
        val key = AesEncryptionHelper.Common.generateAESKey()
        assertNotNull(key)
        assertEquals("AES", key.algorithm)

        val pwKey = AesEncryptionHelper.Common.generateSecureAesKeyFromPassword(password, salt)
        assertNotNull(pwKey)
        assertEquals("AES", pwKey.algorithm)

        val iv = AesEncryptionHelper.Common.getIVSecureRandom().getOrThrow()
        assertNotNull(iv)
        assertEquals(16, iv.iv.size)

        val nonce = AesEncryptionHelper.Common.generateNonce()
        assertEquals(12, nonce.size)
    }

    @Test
    fun testCBCEncryption() {
        val key = AesEncryptionHelper.Common.generateAESKey()
        val iv = AesEncryptionHelper.Common.getIVSecureRandom().getOrThrow()

        // Direct AES/CBC
        val encryptedResult = AesEncryptionHelper.CBC.encrypt(testPlaintext, key, iv)
        assertTrue(encryptedResult.isSuccess)
        val encryptedBase64 = encryptedResult.getOrThrow()
        assertNotEquals(testPlaintext, encryptedBase64)

        val decryptedResult = AesEncryptionHelper.CBC.decrypt(encryptedBase64, key, iv)
        assertTrue(decryptedResult.isSuccess)
        assertEquals(testPlaintext, decryptedResult.getOrThrow())

        // Helper
        val helperResult = AesEncryptionHelper.CBC.encryptAndGenerateEverything(testPlaintext)
        assertNotNull(helperResult)
        val decryptedHelper = helperResult!!.decrypt().getOrThrow()
        assertEquals(testPlaintext, decryptedHelper)

        // Password Helper
        val pwHelperResult = AesEncryptionHelper.CBC.encryptWithPassword(testPlaintext, password)
        assertNotNull(pwHelperResult)
        val decryptedPwHelper = pwHelperResult!!.decrypt().getOrThrow()
        assertEquals(testPlaintext, decryptedPwHelper)

        // Legacy Password Methods (ECB)
        val legacyPassword = "1234567812345678" // 16 chars
        val encryptedLegacy = AesEncryptionHelper.ECB.encryptWithAesAndPassword(testPlaintext, legacyPassword)
        assertNotNull(encryptedLegacy)
        val decryptedLegacy =
            AesEncryptionHelper.ECB.decryptWithAesAndPassword(encryptedLegacy.getOrThrow(), legacyPassword)
        assertEquals(testPlaintext, decryptedLegacy.getOrThrow())
    }

    @Test
    fun testGCMEncryption() {
        val key = AesEncryptionHelper.Common.generateAESKey()
        val nonce = AesEncryptionHelper.Common.generateNonce()

        // Direct AES/GCM
        val encryptedResult = AesEncryptionHelper.GCM.encrypt(testPlaintext, key, nonce)
        assertTrue(encryptedResult.isSuccess)
        val encryptedBase64 = encryptedResult.getOrThrow()

        val decryptedResult = AesEncryptionHelper.GCM.decrypt(encryptedBase64, key, nonce)
        assertTrue(decryptedResult.isSuccess)
        assertEquals(testPlaintext, decryptedResult.getOrThrow())

        // Helper
        val helperResult = AesEncryptionHelper.GCM.encryptAndGenerateEverything(testPlaintext)
        assertNotNull(helperResult)
        val decryptedHelper = helperResult!!.decrypt().getOrThrow()
        assertEquals(testPlaintext, decryptedHelper)

        // Password Helper
        val pwHelperResult = AesEncryptionHelper.GCM.encryptWithPassword(testPlaintext, password)
        assertNotNull(pwHelperResult)
        val decryptedPwHelper = pwHelperResult!!.decrypt().getOrThrow()
        assertEquals(testPlaintext, decryptedPwHelper)
    }

    @Test
    fun testCompression() {
        val largeText = "Repeat this! ".repeat(100)

        // CBC with compression
        val cbcEncryption = AesEncryptionHelper.CBC.encryptAndGenerateEverything(largeText, compress = true)
        assertNotNull(cbcEncryption)
        assertTrue(cbcEncryption!!.compressed)
        val decryptedCbc = cbcEncryption.decrypt().getOrThrow()
        assertEquals(largeText, decryptedCbc)

        // GCM with compression
        val gcmEncryption = AesEncryptionHelper.GCM.encryptAndGenerateEverything(largeText, compress = true)
        assertNotNull(gcmEncryption)
        assertTrue(gcmEncryption!!.compressedBeforeEncryption)
        val decryptedGcm = gcmEncryption.decrypt().getOrThrow()
        assertEquals(largeText, decryptedGcm)
    }

    @Test
    fun testErrorHandling() {
        val key = AesEncryptionHelper.Common.generateAESKey()
        val iv = AesEncryptionHelper.Common.getIVSecureRandom().getOrThrow()
        val nonce = AesEncryptionHelper.Common.generateNonce()

        // Invalid Base64 for decryption
        val invalidBase64 = "NotBase64!!!"

        val cbcResult = AesEncryptionHelper.CBC.decrypt(invalidBase64, key, iv)
        assertTrue(cbcResult.isFailure)

        val gcmResult = AesEncryptionHelper.GCM.decrypt(invalidBase64, key, nonce)
        assertTrue(gcmResult.isFailure)

        // Wrong password length for direct methods
        assertThrows<IllegalArgumentException> {
            AesEncryptionHelper.ECB.encryptWithAesAndPassword(testPlaintext, "short").getOrThrow()
        }
    }
}
