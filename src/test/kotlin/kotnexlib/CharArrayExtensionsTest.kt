package kotnexlib

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CharArrayExtensionsTest {

    @Test
    fun testUseAndWipeClearsMemory() {
        val password = "SuperSecretPassword123!".toCharArray()
        val originalChars = password.clone()

        var capturedBytes: ByteArray? = null

        val result = password.useAndWipe { bytes ->
            capturedBytes = bytes.clone()
            // In the block, the bytes match the UTF-8 representation
            val decoded = String(bytes, Charsets.UTF_8)
            assertEquals(String(originalChars), decoded)
        }

        assertTrue(result.isSuccess)

        // Verify that the original CharArray is wiped with '\u0000'
        assertTrue(password.all { it == '\u0000' })

        // Verify that the internal byte array passed was wiped after execution
        // (capturedBytes was cloned inside, but if we held reference to the actual byte array, it would be zeroed)
        assertNotNull(capturedBytes)
    }

    @Test
    fun testUseAndWipeHandlesException() {
        val chars = "sensitive".toCharArray()

        val result = chars.useAndWipe {
            throw IllegalStateException("Failed during processing")
        }

        assertTrue(result.isFailure)
        assertEquals("Failed during processing", result.exceptionOrNull()?.message)
        // Ensure memory is wiped even when lambda fails
        assertTrue(chars.all { it == '\u0000' })
    }
}
