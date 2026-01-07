package kotnexlib.crypto

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class Argon2HelperTest {

    @Test
    fun testHashAndVerify() {
        val password = "mySecurePassword".toCharArray()
        val hash = Argon2Helper.hash(password)

        assertNotNull(hash)
        assertTrue(hash.isNotEmpty())

        val result = Argon2Helper.verify(password, hash)
        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow())
    }

    @Test
    fun testVerifyWrongPassword() {
        val password = "mySecurePassword".toCharArray()
        val wrongPassword = "wrongPassword".toCharArray()
        val hash = Argon2Helper.hash(password)

        val result = Argon2Helper.verify(wrongPassword, hash)
        assertTrue(result.isSuccess)
        assertFalse(result.getOrThrow())
    }

    @Test
    fun testMobileHash() {
        val password = "mobilePassword".toCharArray()
        val hash = Argon2Helper.hashForMobileDevices(password)

        val result = Argon2Helper.verify(password, hash)
        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow())
    }

    @Test
    fun testCustomParameters() {
        val password = "customPassword".toCharArray()
        val hash = Argon2Helper.hash(password, iterations = 2, memoryKb = 8192, parallelism = 2)

        val result = Argon2Helper.verify(password, hash)
        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow())
    }

    @Test
    fun testWeakParameters() {
        val password = "password".toCharArray()
        assertThrows(IllegalArgumentException::class.java) {
            Argon2Helper.hash(password, iterations = 0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            Argon2Helper.hash(password, memoryKb = 1024)
        }
    }
}
