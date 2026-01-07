package kotnexlib

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PermutationsTest {

    @Test
    fun testCreatePermutations() {
        val chars = charArrayOf('a', 'b')
        val result = chars.createPermutations(length = 2)

        // a^b = 2^2 = 4
        assertEquals(4, result.result.size)
        assertTrue(result.result.containsAll(listOf("aa", "ab", "ba", "bb")))
    }

    @Test
    fun testCreatePermutationsDifferentLength() {
        val chars = charArrayOf('a', 'b', 'c')
        val result = chars.createPermutations(length = 2)

        // 3^2 = 9
        assertEquals(9, result.result.size)
        assertTrue(result.result.contains("aa"))
        assertTrue(result.result.contains("cc"))
    }

    @Test
    fun testOnEachGeneration() {
        val chars = charArrayOf('a', 'b')
        val generated = mutableListOf<String>()
        chars.createPermutations(length = 2) {
            generated.add(it)
            false // don't stop
        }
        assertEquals(4, generated.size)
    }

    @Test
    fun testStopGeneration() {
        val chars = charArrayOf('a', 'b', 'c')
        val generated = mutableListOf<String>()
        chars.createPermutations(length = 3) {
            generated.add(it)
            generated.size == 5 // stop after 5
        }
        assertEquals(5, generated.size)
    }

    @Test
    fun testMultiThreadedPermutations() {
        val chars = charArrayOf('a', 'b')
        val results = chars.createPermutationsMulti(from = 1, until = 2)

        assertEquals(2, results.size)
        assertEquals(2, results[1]?.result?.size) // a, b
        assertEquals(4, results[2]?.result?.size) // aa, ab, ba, bb
    }
}
