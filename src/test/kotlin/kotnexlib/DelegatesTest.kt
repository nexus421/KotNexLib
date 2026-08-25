package kotnexlib

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.properties.Delegates

class DelegatesTest {

    class OnceDemo(val throwOnChange: Boolean = true, initial: String? = null) {
        var value: String by Delegates.once(throwOnChangeTry = throwOnChange, initialValue = initial)
    }

    class OnceOrNullDemo(val throwOnChange: Boolean = true) {
        var value: String? by Delegates.onceOrNull(throwOnChangeTry = throwOnChange)
    }

    @Test
    fun testOnceWithoutInitialValue() {
        val demo = OnceDemo()
        assertThrows(IllegalStateException::class.java) {
            val unread = demo.value
        }

        demo.value = "first"
        assertEquals("first", demo.value)

        assertThrows(IllegalStateException::class.java) {
            demo.value = "second"
        }
    }

    @Test
    fun testOnceWithInitialValue() {
        val demo = OnceDemo(initial = "init")
        assertEquals("init", demo.value)
    }

    @Test
    fun testOnceWithInitialValueRejectsFurtherChanges() {
        // Regression test: a non-null constructor-provided initialValue must count as "already set".
        // The old implementation compared the current value against the original initialValue reference,
        // so as long as the property was never reassigned to something else, it could be "set" again and
        // again without ever throwing - defeating the "once" guarantee whenever initialValue was non-null.
        val demo = OnceDemo(initial = "init")
        assertThrows(IllegalStateException::class.java) {
            demo.value = "changed"
        }
        assertEquals("init", demo.value)
    }

    @Test
    fun testOnceOrNull() {
        val demo = OnceOrNullDemo()
        assertNull(demo.value)

        demo.value = "assigned"
        assertEquals("assigned", demo.value)

        assertThrows(IllegalStateException::class.java) {
            demo.value = "second"
        }
    }

    @Test
    fun testOnceOrNullRejectNullAssignment() {
        val demo = OnceOrNullDemo()
        assertThrows(IllegalStateException::class.java) {
            demo.value = null
        }
    }
}
