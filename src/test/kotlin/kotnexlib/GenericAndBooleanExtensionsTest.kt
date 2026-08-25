package kotnexlib

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.coroutines.cancellation.CancellationException

class GenericAndBooleanExtensionsTest {

    @Test
    fun testIfNull() {
        val nullableString: String? = null
        val result1 = nullableString.ifNull({ "was null" }) { "not null: $this" }
        assertEquals("was null", result1)

        val nonNullString: String = "hello"
        val result2 = nonNullString.ifNull({ "was null" }) { "not null: $this" }
        assertEquals("not null: hello", result2)

        var calledNull = false
        nullableString.ifNull { calledNull = true }
        assertTrue(calledNull)

        var calledNotNull = false
        nonNullString.ifNotNull { calledNotNull = true }
        assertTrue(calledNotNull)
    }

    @Test
    fun testIsNullAndNotNullContracts() {
        val nullStr: String? = null
        assertTrue(nullStr.isNull())
        assertFalse(nullStr.notNull())

        val nonNullStr: String = "test"
        assertFalse(nonNullStr.isNull())
        assertTrue(nonNullStr.notNull())

        // Smart cast verification
        if (nonNullStr.notNull()) {
            assertEquals(4, nonNullStr.length) // Smart-cast to non-null
        }

        if (!nullStr.isNull()) {
            // Smart cast when isNull() is false
            assertEquals(0, nullStr.length)
        }
    }

    @Test
    fun testOnNull() {
        var sideEffect = false
        val nullVal: String? = null
        val res1 = nullVal.onNull { sideEffect = true }
        assertNull(res1)
        assertTrue(sideEffect)

        var sideEffect2 = false
        val nonNullVal: String = "test"
        val res2 = nonNullVal.onNull { sideEffect2 = true }
        assertEquals("test", res2)
        assertFalse(sideEffect2)
    }

    @Test
    fun testDefault() {
        val nullVal: String? = null
        assertEquals("fallback", nullVal.default("fallback"))

        val presentVal: String = "exists"
        assertEquals("exists", presentVal.default("fallback"))
    }

    @Test
    fun testTryOrNullAndTryOrDefault() {
        var errorReported: Throwable? = null
        val failingResult = tryOrNull(onError = { errorReported = it }) {
            throw IllegalArgumentException("boom")
        }
        assertNull(failingResult)
        assertNotNull(errorReported)
        assertEquals("boom", errorReported?.message)

        val successResult = tryOrNull { "success" }
        assertEquals("success", successResult)

        val defaultRes = tryOrDefault(default = "fallback") {
            throw RuntimeException("error")
        }
        assertEquals("fallback", defaultRes)

        val receiverObj: String? = null
        val receiverDefault = receiverObj.tryOrDefault(default = "fallback") { length }
        assertEquals("fallback", receiverDefault)
    }

    @Test
    fun testTryOrNullRethrowsCancellationException() {
        assertThrows(CancellationException::class.java) {
            tryOrNull {
                throw CancellationException("cancelled")
            }
        }
    }

    @Test
    fun testBooleanExtensions() {
        var executedTrue = false
        true.ifTrue { executedTrue = true }
        assertTrue(executedTrue)

        var executedFalse = false
        false.ifFalse { executedFalse = true }
        assertTrue(executedFalse)

        val nullBool: Boolean? = null
        assertTrue(nullBool.orTrue)
        assertFalse(nullBool.orFalse)

        assertEquals("ja", true.toGerman())
        assertEquals("Nein", false.toGerman(startUpperCase = true))
        assertEquals("yes", true.toEnglish())
        assertEquals("No", false.toEnglish(startUpperCase = true))

        assertEquals("on", true.switchText("on", "off"))
        assertEquals("off", false.switchText("on", "off"))

        val order = mutableListOf<Int>()
        true.switchOrder({ order.add(1) }, { order.add(2) })
        assertEquals(listOf(1, 2), order)

        val reverseOrder = mutableListOf<Int>()
        false.switchOrder({ reverseOrder.add(1) }, { reverseOrder.add(2) })
        assertEquals(listOf(2, 1), reverseOrder)
    }

    sealed class SealedDemo {
        class SubA : SealedDemo()
        data class SubB(val x: Int) : SealedDemo()
        data object SubC : SealedDemo()
    }

    @Test
    fun testGetAllSealedSubclasses() {
        val subclasses = getAllSealedSubclassesFrom(SealedDemo::class)
        assertEquals(3, subclasses.size)
        val names = subclasses.map { it.simpleName }.toSet()
        assertTrue(names.contains("SubA"))
        assertTrue(names.contains("SubB"))
        assertTrue(names.contains("SubC"))
    }
}
