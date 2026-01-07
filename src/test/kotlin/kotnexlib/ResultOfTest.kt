package kotnexlib

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ResultOfTest {

    @Test
    fun testResultOf() {
        val success: ResultOf<String> = ResultOf.Success("OK")
        assertTrue(success.isSuccess)
        assertFalse(success.isFailure)
        assertEquals("OK", success.getOrNull())

        val failure: ResultOf<String> = ResultOf.Failure("Error", RuntimeException())
        assertTrue(failure.isFailure)
        assertFalse(failure.isSuccess)
        assertNull(failure.getOrNull())
    }

    @Test
    fun testResultOf2() {
        val success: ResultOf2<Int, String> = ResultOf2.Success(123)
        assertTrue(success.isSuccess)
        assertEquals(123, success.getOrNull())

        val failure: ResultOf2<Int, String> = ResultOf2.Failure("Failed")
        assertTrue(failure.isFailure)
        assertEquals("Failed", (failure as ResultOf2.Failure).value)
    }

    @Test
    fun testResultOfTripple() {
        val success: ResultOfTripple<Int, String, Double> = ResultOfTripple.Success(1)
        assertTrue(success.isSuccess)

        val warning: ResultOfTripple<Int, String, Double> = ResultOfTripple.Warning("Watch out")
        assertTrue(warning.isWarning)

        val error: ResultOfTripple<Int, String, Double> = ResultOfTripple.Error(404.0)
        assertTrue(error.isError)
    }
}
