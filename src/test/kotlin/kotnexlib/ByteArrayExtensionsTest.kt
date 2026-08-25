package kotnexlib

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.text.toHexString as stdlibToHexString

class ByteArrayExtensionsTest {

    @Test
    fun testToBase64() {
        val bytes = "Hello".toByteArray()
        val expected = "SGVsbG8="
        assertEquals(expected, bytes.toBase64())
    }

    @Test
    fun testCompressionDecompression() {
        val original = "Compress me if you can".repeat(50).toByteArray()
        val compressed = original.compress()
        assertNotNull(compressed)
        assertNotEquals(original.size, compressed!!.size)

        val decompressed = compressed.decompress()
        assertNotNull(decompressed)
        assertArrayEquals(original, decompressed)
    }

    @Test
    fun testEmptyCompression() {
        val empty: ByteArray = byteArrayOf()
        assertArrayEquals(empty, empty.compress())
        assertArrayEquals(empty, empty.decompress())

        val nullBytes: ByteArray? = null
        assertNull(nullBytes.compress())
        assertNull(nullBytes.decompress())
    }

    @Suppress("DEPRECATION")
    @Test
    fun testToHexString() {
        val bytes = byteArrayOf(0x0A, 0xFF.toByte(), 0x00, 0x1B)
        assertEquals("0aff001b", bytes.toHexString())
    }

    @Suppress("DEPRECATION")
    @Test
    fun testToHexStringMatchesStdlib() {
        val bytes = byteArrayOf(0x00, 0x7F, 0xFF.toByte(), 0x10, 0xA5.toByte())

        // bytes.toHexString() resolves to this library's deprecated version (same-package
        // declarations take priority over kotlin.text's default-imported one), so the stdlib
        // version is imported under an alias to force the comparison.
        assertEquals(bytes.stdlibToHexString(), bytes.toHexString())
    }
}
