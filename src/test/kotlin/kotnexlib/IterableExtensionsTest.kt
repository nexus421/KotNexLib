package kotnexlib

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class IterableExtensionsTest {

    @Test
    fun testForEachDoLast() {
        val list = listOf("a", "b", "c")
        val results = mutableListOf<Pair<String, Boolean>>()
        list.forEachDoLast { s, isLast ->
            results.add(s to isLast)
        }

        assertEquals(3, results.size)
        assertFalse(results[0].second)
        assertFalse(results[1].second)
        assertTrue(results[2].second)
    }

    @Test
    fun testSplitFilter() {
        val list = listOf(1, 2, 3, 4, 5, 6)
        val split = list.splitFilter { it % 2 == 0 }

        assertEquals(listOf(2, 4, 6), split.trueList)
        assertEquals(listOf(1, 3, 5), split.falseList)
    }

    @Test
    fun testHandleSizes() {
        val empty = emptyList<String>()
        assertTrue(empty.handleSizes() is IterableSize.IsEmpty)

        val one = listOf("one")
        val handleOne = one.handleSizes()
        assertTrue(handleOne is IterableSize.IsOne)
        assertEquals("one", (handleOne as IterableSize.IsOne).entry)

        val many = listOf("a", "b")
        assertTrue(many.handleSizes() is IterableSize.HasMany)
    }

    @Test
    fun testMove() {
        val list = mutableListOf("a", "b", "c")
        list.move("a", 2)
        assertEquals(listOf("b", "c", "a"), list)

        list.move("c", 0)
        assertEquals(listOf("c", "b", "a"), list)

        list.move("nonexistent", 1) // Nothing should happen
        assertEquals(listOf("c", "b", "a"), list)
    }

    @Test
    fun testIsBeforeAfter() {
        val list = listOf("first", "second", "third")
        assertTrue(list.isBefore("first") { it == "second" })
        assertFalse(list.isBefore("third") { it == "second" })

        assertTrue(list.isAfter("third") { it == "first" })
        assertFalse(list.isAfter("first") { it == "third" })
    }
}
