package kotnexlib.crypto

import kotnexlib.CriticalAPI
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

@OptIn(CriticalAPI::class)
class BlowfishEncryptionHelperTest {

    @Test
    fun testBlowfishEncryptionDecryption() {
        val originalText = "Hello Blowfish!"
        val password = "secretPassword"

        val encrypted = BlowfishEncryptionHelper.encryptWithBlowfish(originalText, password)
        assertNotNull(encrypted)

        val decrypted = BlowfishEncryptionHelper.decryptWithBlowfish(encrypted!!, password)
        assertEquals(originalText, decrypted)
    }

    @Test
    fun testBlowfishWithCompression() {
        val largeText = "A".repeat(1000)
        val password = "secretPassword"

        val encrypted = BlowfishEncryptionHelper.encryptWithBlowfish(largeText, password, compress = true)
        assertNotNull(encrypted)

        val decrypted = BlowfishEncryptionHelper.decryptWithBlowfish(encrypted!!, password, isCompressed = true)
        assertEquals(largeText, decrypted)
    }

    @Test
    fun testBlowfishWrongPassword() {
        val originalText = "Hello Blowfish!"
        val password = "secretPassword"
        val wrongPassword = "wrongPassword"

        val encrypted = BlowfishEncryptionHelper.encryptWithBlowfish(originalText, password)
        assertNotNull(encrypted)

        // Decryption with wrong password should fail or return wrong result
        val decrypted = BlowfishEncryptionHelper.decryptWithBlowfish(encrypted!!, wrongPassword)
        assertNotEquals(originalText, decrypted)
    }

    @Test
    fun testBlowfishByteArray() {
        val originalBytes = "Some bytes".toByteArray()
        val password = "password"

        val encryptedResult = BlowfishEncryptionHelper.encryptWithBlowfish(originalBytes, password)
        assertTrue(encryptedResult.isSuccess)

        val decryptedResult = BlowfishEncryptionHelper.decryptWithBlowfish(encryptedResult.getOrThrow(), password)
        assertTrue(decryptedResult.isSuccess)
        assertArrayEquals(originalBytes, decryptedResult.getOrThrow())
    }
}
