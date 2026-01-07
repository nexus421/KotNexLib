package kotnexlib

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MathTest {

    @Test
    fun testDotProduct() {
        val vectorA = listOf(1.0, 2.0, 3.0)
        val vectorB = listOf(4.0, 5.0, 6.0)
        // 1*4 + 2*5 + 3*6 = 4 + 10 + 18 = 32
        assertEquals(32.0, Math.dotProduct(vectorA, vectorB))
    }

    @Test
    fun testCosineSimilarity() {
        val vectorA = listOf(1.0, 0.0)
        val vectorB = listOf(0.0, 1.0)
        // Orthogonal vectors should have 0 similarity
        assertEquals(0.0, Math.cosineSimilarity(vectorA, vectorB), 1e-9)

        val vectorC = listOf(1.0, 1.0)
        val vectorD = listOf(2.0, 2.0)
        // Parallel vectors should have 1 similarity
        assertEquals(1.0, Math.cosineSimilarity(vectorC, vectorD), 1e-9)
    }

    @Test
    fun testNormalizeVector() {
        val vector = listOf(3.0, 4.0)
        // Length is sqrt(3^2 + 4^2) = 5
        // Normalized: (3/5, 4/5) = (0.6, 0.8)
        val normalized = with(Math) { vector.normalizeVector() }
        assertEquals(0.6, normalized[0], 1e-9)
        assertEquals(0.8, normalized[1], 1e-9)

        with(Math) {
            assertTrue(normalized.isNormalizedVector())
        }
    }

    @Test
    fun testPowOfTwo() {
        assertEquals(16.0, 4.0.powOfTwo())
        assertEquals(25, 5.powOfTwo())
        assertEquals(100L, 10L.powOfTwo())
    }
}
