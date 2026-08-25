@file:OptIn(ExperimentalKotNexLibAPI::class)

package kotnexlib.external.objectbox

import kotnexlib.ExperimentalKotNexLibAPI
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ObjectBoxAESTest {

    @AfterEach
    fun resetState() {
        CryptoStringEncryptionWithPassword.password = ""
        CryptoStringEncryptionWithPassword.clearKeyCache()
    }

    @Test
    fun testRoundtrip() {
        CryptoStringEncryptionWithPassword.password = "unit-test-password"
        val stored = CryptoStringEncryptionWithPassword.convertToDatabaseValue("secret value")
        val restored = CryptoStringEncryptionWithPassword.convertToEntityProperty(stored)
        assertEquals("secret value", restored)
    }

    @Test
    fun testBlankPasswordThrows() {
        CryptoStringEncryptionWithPassword.password = ""
        assertThrows(IllegalStateException::class.java) {
            CryptoStringEncryptionWithPassword.convertToDatabaseValue("x")
        }
    }

    @Test
    fun testGetOrDeriveKeyCachesForSameSaltAndPassword() {
        val salt = ByteArray(16) { it.toByte() }
        val key1 = CryptoStringEncryptionWithPassword.getOrDeriveKey("pw", salt)
        val key2 = CryptoStringEncryptionWithPassword.getOrDeriveKey("pw", salt)
        assertSame(key1, key2) // same cached instance, no re-derivation
    }

    @Test
    fun testGetOrDeriveKeyDistinguishesSaltAndPassword() {
        val salt1 = ByteArray(16) { it.toByte() }
        val salt2 = ByteArray(16) { (it + 1).toByte() }

        val bySalt = CryptoStringEncryptionWithPassword.getOrDeriveKey("pw", salt1)
        val byOtherSalt = CryptoStringEncryptionWithPassword.getOrDeriveKey("pw", salt2)
        val byOtherPassword = CryptoStringEncryptionWithPassword.getOrDeriveKey("otherPw", salt1)

        assertNotSame(bySalt, byOtherSalt)
        assertNotSame(bySalt, byOtherPassword)
    }

    @Test
    fun testClearKeyCacheForcesRederivation() {
        val salt = ByteArray(16) { it.toByte() }
        val before = CryptoStringEncryptionWithPassword.getOrDeriveKey("pw", salt)
        CryptoStringEncryptionWithPassword.clearKeyCache()
        val after = CryptoStringEncryptionWithPassword.getOrDeriveKey("pw", salt)

        assertNotSame(before, after) // re-derived, not served from the (cleared) cache
        assertEquals(before.encoded.toList(), after.encoded.toList()) // deterministic: same key material
    }
}
