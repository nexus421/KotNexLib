package kotnexlib

import kotnexlib.crypto.HashAlgorithm
import kotnexlib.crypto.hash
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

class StringExtensionsTest {

    @Test
    fun testCoverString() {
        val original = "12345678"
        // Default: start=1, end=length-2 (6), char='*'
        assertEquals("1******8", original.coverString())
        assertEquals("123**678", original.coverString(3, 4))
        assertThrows<IllegalArgumentException>(IllegalArgumentException::class.java) { original.coverString(0, 0) }
    }

    @Test
    fun testHash() {
        val text = "hello"
        val md5 = text.hash(HashAlgorithm.MD5)
        assertEquals("5d41402abc4b2a76b9719d911017c592", md5)

        val sha256 = text.hash(HashAlgorithm.SHA_256)
        assertEquals("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824", sha256)
    }

    @Test
    fun testReplaceAllMatchingStart() {
        assertEquals("12340", "00012340".replaceAllMatchingStart('0'))
        assertEquals("abc", "xxxabc".replaceAllMatchingStart('x'))
        assertEquals("", "aaaaa".replaceAllMatchingStart('a'))
    }

    @Test
    fun testCompression() {
        val largeText = "Hello World! ".repeat(100)
        val compressed = largeText.compress()
        assertNotNull(compressed)
        val decompressed = compressed.decompress()
        assertEquals(largeText, decompressed)
    }

    @Test
    fun testValidationExtensions() {
        assertTrue("12345".isDigitOnly)
        assertFalse("123a5".isDigitOnly)

        assertTrue("abcABC".isAlphabeticOnly)
        assertFalse("abc1ABC".isAlphabeticOnly)

        assertTrue("abc123".isAlphanumericOnly)
        assertFalse("abc 123".isAlphanumericOnly)
    }

    @Test
    fun testToDate() {
        val dateStr = "01.01.2024"
        val date = dateStr.toDateOrNull()
        assertNotNull(date)

        val calendar = Calendar.getInstance()
        calendar.time = date!!
        assertEquals(2024, calendar.get(Calendar.YEAR))
        assertEquals(Calendar.JANUARY, calendar.get(Calendar.MONTH))
        assertEquals(1, calendar.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun testContainsOneOf() {
        val text = "The quick brown fox"
        val list = listOf("dog", "fox", "cat")
        assertTrue(text.containsOneOf(list))
        assertEquals("fox", text.containsOneOfAndGet(list))
        assertFalse(text.containsOneOf(listOf("dog", "cat")))
    }

    @Test
    fun testBase64() {
        val original = "Hello World"
        val encoded = original.toBase64()
        assertEquals("SGVsbG8gV29ybGQ=", encoded)
        assertEquals(original, encoded.fromBase64())
    }
}
