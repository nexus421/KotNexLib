package kotnexlib

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class NumberExtensionsTest {

    @Test
    fun testNegative() {
        assertEquals(-5, 5.negative())
        assertEquals(-5, (-5).negative())
        assertEquals(0, 0.negative())

        assertEquals(-5.5, 5.5.negative(), 0.001)
        assertEquals(-5.5, (-5.5).negative(), 0.001)

        assertEquals(-5L, 5L.negative())
        assertEquals(-5f, 5f.negative(), 0.001f)
    }

    @Test
    fun testIsBetween() {
        assertTrue(5.isBetween(1, 10))
        assertFalse(1.isBetween(1, 10)) // Strict inequality: lower < this < higher
        assertFalse(10.isBetween(1, 10))
        assertFalse(15.isBetween(1, 10))

        assertTrue(5.0.isBetween(1.0, 10.0))
        assertFalse(1.0.isBetween(1.0, 10.0))
    }

    @Test
    @OptIn(ExperimentalStdlibApi::class)
    fun testConvertStorageUnits() {
        val oneMb = 1L
        val inBytes = oneMb.convert(from = ConvertType.MegaByte, to = ConvertType.Byte)
        assertEquals(1_000_000.0, inBytes, 0.001)

        val inKb = oneMb.convert(from = ConvertType.MegaByte, to = ConvertType.KiloByte)
        assertEquals(1000.0, inKb, 0.001)

        val oneGb = 1.convert(from = ConvertType.GigaByte, to = ConvertType.MegaByte)
        assertEquals(1000.0, oneGb, 0.001)

        val bytesToGb = 2_000_000_000L.convert(from = ConvertType.Byte, to = ConvertType.GigaByte)
        assertEquals(2.0, bytesToGb, 0.001)
    }

    @Test
    fun testPowOfTwo() {
        assertEquals(25.0, 5.0.powOfTwo(), 0.001)
        assertEquals(25, 5.powOfTwo())
        assertEquals(25L, 5L.powOfTwo())
        assertEquals(25f, 5f.powOfTwo(), 0.001f)
    }
}
