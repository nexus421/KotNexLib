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

        val encrypted = BlowfishEncryption.encrypt(originalText, password)
        assertNotNull(encrypted)

        val decrypted = BlowfishEncryption.decrypt(encrypted!!, password)
        assertEquals(originalText, decrypted)
    }

    @Test
    fun testBlowfishWithCompression() {
        val largeText = "A".repeat(1000)
        val password = "secretPassword"

        val encrypted = BlowfishEncryption.encrypt(largeText, password, compress = true)
        assertNotNull(encrypted)

        val decrypted = BlowfishEncryption.decrypt(encrypted!!, password, isCompressed = true)
        assertEquals(largeText, decrypted)
    }

    @Test
    fun testBlowfishWrongPassword() {
        val originalText = "Hello Blowfish!"
        val password = "secretPassword"
        val wrongPassword = "wrongPassword"

        val encrypted = BlowfishEncryption.encrypt(originalText, password)
        assertNotNull(encrypted)

        // Decryption with wrong password should fail or return wrong result
        val decrypted = BlowfishEncryption.decrypt(encrypted!!, wrongPassword)
        assertNotEquals(originalText, decrypted)
    }

    @Test
    fun testBlowfishByteArray() {
        val originalBytes = "Some bytes".toByteArray()
        val password = "password"

        val encryptedResult = BlowfishEncryption.encrypt(originalBytes, password)
        assertTrue(encryptedResult.isSuccess)

        val decryptedResult = BlowfishEncryption.decrypt(encryptedResult.getOrThrow(), password)
        assertTrue(decryptedResult.isSuccess)
        assertArrayEquals(originalBytes, decryptedResult.getOrThrow())
    }
}
