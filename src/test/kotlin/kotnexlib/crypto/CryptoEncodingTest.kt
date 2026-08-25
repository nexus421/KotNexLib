package kotnexlib.crypto

import kotnexlib.ExperimentalKotNexLibAPI
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

@OptIn(ExperimentalKotNexLibAPI::class)
class CryptoEncodingTest {

    @Test
    fun testAesGcmWithSpecialCharactersAndUmlauts() {
        val originalText = "Äpfel, Übergrößen, Süßigkeiten & 🚀 Emojis! 12345 €"
        val password = "PasswörtMitSönderzeichen123!?"

        val encryptedData = AES.GCM.encryptWithPassword(originalText, password)
        assertNotNull(encryptedData)

        val decrypted = encryptedData.decryptAsString(password).getOrNull()
        assertEquals(originalText, decrypted)
    }

    @Test
    fun testAesCbcWithSpecialCharactersAndUmlauts() {
        val originalText = "Große Änderungen in München, Köln & Zürich! 🔑"
        val password = "StarkesPasswort#999"

        val encryptedData = AES.CBC.encryptWithPassword(originalText, password)
        assertNotNull(encryptedData)

        val decrypted = encryptedData?.decryptAsString(password)?.getOrNull()
        assertEquals(originalText, decrypted)
    }

    @Test
    fun testAesDataSerializationRoundtrip() {
        val originalText = "Test Serialization with AESData"
        val password = "mySecretPassword123"

        val encrypted = AES.GCM.encryptWithPassword(originalText, password)
        val serializedString = encrypted.toString()

        val restored = AES.AESData.restore(serializedString).getOrNull()
        assertNotNull(restored)

        val decrypted = restored?.decryptAsString(password)?.getOrNull()
        assertEquals(originalText, decrypted)
    }

    @Test
    fun testHashBCWithSpecialCharacters() {
        val text = "München und Köln"
        val sha256 = text.hashBC(HashAlgorithmBC.SHA_256)
        assertNotNull(sha256)
        assertEquals(64, sha256.length)

        val sha3_256 = text.hashBC(HashAlgorithmBC.SHA3_256)
        assertNotNull(sha3_256)
        assertEquals(64, sha3_256.length)
    }

    @Test
    fun testArgon2VerificationWithSpecialCharacters() {
        val password = "ÄpfelÜberAlles!123".toCharArray()
        val hash = Argon2Helper.hash(password)

        val verifySuccess = Argon2Helper.verify(password, hash).getOrNull()
        assertEquals(true, verifySuccess)

        val wrongPassword = "FalschesPasswort".toCharArray()
        val verifyFail = Argon2Helper.verify(wrongPassword, hash).getOrNull()
        assertEquals(false, verifyFail)
    }
}
