package kotnexlib

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.milliseconds

class CacheTest {

    @Test
    fun testBasicCacheOperations() {
        val cache = Cache<String, Int>()
        assertTrue(cache.isEmpty)
        assertEquals(0, cache.size)

        cache.set("a", 100)
        assertEquals(1, cache.size)
        assertEquals(100, cache.get("a"))
        assertNull(cache.get("b"))

        val removed = cache.remove("a")
        assertEquals(100, removed)
        assertTrue(cache.isEmpty)
    }

    @Test
    fun testGetOrSet() = runBlocking {
        val cache = Cache<String, String>()
        var computeCount = 0

        val val1 = cache.getOrSet("user_1") {
            computeCount++
            "Alice"
        }
        assertEquals("Alice", val1)
        assertEquals(1, computeCount)

        val val2 = cache.getOrSet("user_1") {
            computeCount++
            "Bob"
        }
        assertEquals("Alice", val2)
        assertEquals(1, computeCount) // Should hit cache
    }

    @Test
    fun testEvictionCallback() {
        val evictedEntries = mutableListOf<Pair<String, String>>()
        val cache = Cache<String, String>(onEvict = { k, v ->
            evictedEntries.add(k to v)
        })

        cache.set("k1", "v1")
        cache.set("k2", "v2")

        cache.remove("k1")
        assertEquals(listOf("k1" to "v1"), evictedEntries)

        cache.clear()
        assertEquals(listOf("k1" to "v1", "k2" to "v2"), evictedEntries)
    }

    @Test
    fun testManualCleanupByMaxAge() {
        val evictedEntries = mutableListOf<Pair<String, String>>()
        val cache = Cache<String, String>(maxAge = 50.milliseconds, onEvict = { k, v ->
            evictedEntries.add(k to v)
        })

        cache.set("old", "data1")
        Thread.sleep(60)
        cache.set("fresh", "data2")

        cache.cleanup(50.milliseconds)

        assertNull(cache.get("old"))
        assertEquals("data2", cache.get("fresh"))
        assertEquals(listOf("old" to "data1"), evictedEntries)
    }

    data class TestModel(val id: String, val name: String)

    @Test
    fun testLocalCache() = runBlocking {
        val user = TestModel("1", "Marvin")
        LocalCache.setData("m1", user)

        val retrieved = LocalCache.getData<TestModel>("m1")
        assertEquals(user, retrieved)

        val loaded = LocalCache.getOrSet("m2") {
            TestModel("2", "Kadoffe")
        }
        assertEquals("Kadoffe", loaded.name)
    }
}
