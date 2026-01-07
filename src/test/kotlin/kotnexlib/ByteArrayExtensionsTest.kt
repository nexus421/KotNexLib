package kotnexlib

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

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
}
