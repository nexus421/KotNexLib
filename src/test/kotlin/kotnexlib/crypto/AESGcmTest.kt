@file:OptIn(ExperimentalKotNexLibAPI::class)

package kotnexlib.crypto

import kotnexlib.ExperimentalKotNexLibAPI
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class AESGcmTest {

    @Test
    fun testGcmEncryptionDecryption() {
        val originalText = "Hello GCM World!"

        // Test with Password Helper
        val password = "StrongPassword123"
        val pwEncryption = AES.GCM.encryptWithPassword(originalText, password)
        assertNotNull(pwEncryption)
        val decryptedPwResult = pwEncryption.decryptAsString(password)
        assertEquals(originalText, decryptedPwResult.getOrThrow())
    }
}
