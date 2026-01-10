@file:OptIn(ExperimentalKotNexLibAPI::class)

package kotnexlib.crypto

import kotnexlib.ExperimentalKotNexLibAPI
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class AesEncryptionHelperGcmTest {

    @Test
    fun testGcmEncryptionDecryption() {
        val originalText = "Hello GCM World!"

        // Test with Helper
        val encryption = AesEncryptionHelper.GCM.encryptAndGenerateEverything(originalText)
        assertNotNull(encryption)
        val decryptedResult = encryption!!.decrypt()
        assertEquals(originalText, decryptedResult.getOrThrow())

        // Test with Password Helper
        val password = "StrongPassword123"
        val pwEncryption = AesEncryptionHelper.GCM.encryptWithPassword(originalText, password)
        assertNotNull(pwEncryption)
        val decryptedPwResult = pwEncryption!!.decrypt()
        assertEquals(originalText, decryptedPwResult.getOrThrow())
    }

    @Test
    fun testGcmCompression() {
        val largeText = "A".repeat(1000)
        val encryption = AesEncryptionHelper.GCM.encryptAndGenerateEverything(largeText, compress = true)
        assertNotNull(encryption)
        val decryptedResult = encryption.decrypt()
        assertEquals(largeText, decryptedResult.getOrThrow())
    }
}
