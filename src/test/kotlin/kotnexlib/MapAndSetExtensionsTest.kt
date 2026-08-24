package kotnexlib

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class MapAndSetExtensionsTest {

    @Test
    fun testKeysOnlyLeftAndRight() {
        val mapA = mapOf("a" to 1, "b" to 2, "c" to 3)
        val mapB = mapOf("b" to 20, "c" to 3, "d" to 4)

        val leftOnly = mapA.keysOnlyLeft(mapB)
        assertEquals(mapOf("a" to 1), leftOnly)

        val rightOnly = mapA.keysOnlyRight(mapB)
        assertEquals(mapOf("d" to 4), rightOnly)
    }

    @Test
    fun testEntriesDiffering() {
        val mapA = mapOf("a" to 1, "b" to 2, "c" to 3)
        val mapB = mapOf("b" to 20, "c" to 3, "d" to 4)

        val differing = mapA.entriesDiffering(mapB)
        assertEquals(1, differing.size)
        assertTrue(differing.containsKey("b"))
        assertEquals(2, differing["b"]?.valueLeftMap)
        assertEquals(20, differing["b"]?.valueRightMap)
    }

    @Test
    fun testAreEqual() {
        val map1 = mapOf("x" to 10, "y" to 20)
        val map2 = mapOf("x" to 10, "y" to 20)
        val map3 = mapOf("x" to 10, "y" to 99)
        val map4 = mapOf("x" to 10)

        assertTrue(map1.areEqual(map2))
        assertFalse(map1.areEqual(map3))
        assertFalse(map1.areEqual(map4))
    }

    @Test
    fun testSetDifference() {
        val set1 = setOf("apple", "banana", "cherry")
        val set2 = setOf("banana", "dragonfruit")

        val diff = set1.subtract(set2)
        assertEquals(setOf("apple", "cherry"), diff)
    }
}
