package kotnexlib

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

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

    @Suppress("DEPRECATION")
    @Test
    fun testToMillisFrom() {
        assertEquals(60_000L, 60.toMillisFrom(TimeUnit.Second))
        assertEquals(60_000L, 60L.toMillisFrom(TimeUnit.Second))
        assertEquals(5_184_000_000L, 60L.toMillisFrom(TimeUnit.Day))
        assertEquals(3_600_000L, 1L.toMillisFrom(TimeUnit.Hour))
        assertEquals(60_000L, 1L.toMillisFrom(TimeUnit.Minute))
    }

    @Suppress("DEPRECATION")
    @Test
    fun testToMillisFromMatchesStdlibDuration() {
        val value = 42L

        assertEquals(value.days.inWholeMilliseconds, value.toMillisFrom(TimeUnit.Day))
        assertEquals(value.hours.inWholeMilliseconds, value.toMillisFrom(TimeUnit.Hour))
        assertEquals(value.minutes.inWholeMilliseconds, value.toMillisFrom(TimeUnit.Minute))
        assertEquals(value.seconds.inWholeMilliseconds, value.toMillisFrom(TimeUnit.Second))

        assertEquals(value.seconds.inWholeMilliseconds, value.toInt().toMillisFrom(TimeUnit.Second))
    }
}
