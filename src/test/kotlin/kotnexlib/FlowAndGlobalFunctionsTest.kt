package kotnexlib

import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.time.Duration

class FlowAndGlobalFunctionsTest {

    @Test
    fun testTimeAndEmit() = runBlocking {
        // Collect first 2 emissions with 10 emissions per second (100ms interval)
        val emissions = timeAndEmit(emissionsPerSecond = 10).take(2).toList()
        assertEquals(2, emissions.size)
        assertEquals(Duration.ZERO, emissions[0])
        assertTrue(emissions[1].inWholeMilliseconds >= 50)
    }

    @Test
    fun testDoUntilNotNull() {
        var attempts = 0
        val result = doUntilNotNull(onNull = { attempts++ }) {
            if (attempts >= 3) "ready" else null
        }
        assertEquals("ready", result)
        assertEquals(3, attempts)
    }

    @Test
    fun testMeasureTimeSilent() {
        var measuredMs: Long = -1
        val result = measureTimeSilent(time = { measuredMs = it }) {
            Thread.sleep(20)
            42
        }
        assertEquals(42, result)
        assertTrue(measuredMs >= 15)
    }

    @Test
    fun testRetryOnError() = runBlocking {
        var tries = 0
        val res = retryOnError(attempts = 3, waitBetweenRetryMillis = 10) {
            tries++
            if (tries < 2) throw RuntimeException("fail")
            "success"
        }
        assertEquals("success", res)
        assertEquals(2, tries)
    }

    @Test
    fun testRetryRethrowsCancellationException() {
        assertThrows(kotlin.coroutines.cancellation.CancellationException::class.java) {
            runBlocking {
                retryOnError(attempts = 3, waitBetweenRetryMillis = 10) {
                    throw kotlin.coroutines.cancellation.CancellationException("cancelled")
                }
            }
        }
    }

    @Test
    fun testCalcBcc() {
        // Default ignoreFirstCharacter=true skips the first character (e.g. a leading STX/SOH control char).
        assertEquals("0", "00".calcBcc())
        assertEquals("0", "0".calcBcc(ignoreFirstCharacter = false))
    }

    @Test
    fun testCalcBccThrowsOnTooShortString() {
        // Regression test: must fail with a clear, controlled exception instead of an uncontrolled
        // StringIndexOutOfBoundsException for strings shorter than what the algorithm requires.
        assertThrows(IllegalArgumentException::class.java) { "".calcBcc() }
        assertThrows(IllegalArgumentException::class.java) { "a".calcBcc() }
        assertThrows(IllegalArgumentException::class.java) { "".calcBcc(ignoreFirstCharacter = false) }
    }
}
