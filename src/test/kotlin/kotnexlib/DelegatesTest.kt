package kotnexlib

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.properties.Delegates

class DelegatesTest {

    class OnceDemo(
        val throwOnChange: Boolean = true,
        initial: String? = null,
        onValueChanged: (() -> Unit)? = null
    ) {
        var value: String by Delegates.once(
            throwOnChangeTry = throwOnChange,
            initialValue = initial,
            onValueChanged = onValueChanged
        )
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
        // A rejected change must not have modified the stored value.
        assertEquals("first", demo.value)
    }

    @Test
    fun testOnceWithoutInitialValue_secondSetSilentlyIgnoredWhenNotThrowing() {
        val demo = OnceDemo(throwOnChange = false)
        demo.value = "first"

        assertDoesNotThrow {
            demo.value = "second"
        }
        assertEquals("first", demo.value)
    }

    @Test
    fun testOnceWithInitialValue() {
        val demo = OnceDemo(initial = "init")
        // Providing a non-null initialValue means the getter must never throw, even before an explicit set.
        assertEquals("init", demo.value)
    }

    @Test
    fun testOnceWithInitialValue_settingTheSameValueRepeatedlyIsAlwaysAllowed() {
        // Intended contract: as long as the stored value has not actually diverged from initialValue,
        // it may be assigned again and again without error - only a genuine CHANGE away from initialValue
        // is a one-time event. Do not "fix" this into an isSet-flag that locks on construction; that was
        // tried before and broke production usages that legitimately re-apply the same default value.
        val demo = OnceDemo(initial = "init")

        repeat(100) {
            assertDoesNotThrow { demo.value = "init" }
        }
        assertEquals("init", demo.value)
    }

    @Test
    fun testOnceWithInitialValue_firstRealChangeSucceeds() {
        val demo = OnceDemo(initial = "init")
        demo.value = "changed"
        assertEquals("changed", demo.value)
    }

    @Test
    fun testOnceWithInitialValue_repeatedIdempotentSetsThenOneRealChangeSucceeds() {
        val demo = OnceDemo(initial = "init")
        repeat(10) { demo.value = "init" }

        demo.value = "changed"
        assertEquals("changed", demo.value)
    }

    @Test
    fun testOnceWithInitialValue_secondRealChangeIsRejected() {
        val demo = OnceDemo(initial = "init")
        demo.value = "changed"

        assertThrows(IllegalStateException::class.java) {
            demo.value = "changed again"
        }
        assertEquals("changed", demo.value)
    }

    @Test
    fun testOnceWithInitialValue_cannotRevertToInitialValueAfterChanging() {
        // Once the value has genuinely changed away from initialValue, even setting it back to the
        // original initialValue counts as a further change attempt and must be rejected.
        val demo = OnceDemo(initial = "init")
        demo.value = "changed"

        assertThrows(IllegalStateException::class.java) {
            demo.value = "init"
        }
        assertEquals("changed", demo.value)
    }

    @Test
    fun testOnceWithInitialValue_furtherChangeSilentlyIgnoredWhenNotThrowing() {
        val demo = OnceDemo(throwOnChange = false, initial = "init")
        demo.value = "changed"

        assertDoesNotThrow {
            demo.value = "changed again"
        }
        assertEquals("changed", demo.value)
    }

    @Test
    fun testOnceWithoutInitialValue_onValueChangedInvokedOnlyOnSuccessfulSet() {
        var callCount = 0
        val demo = OnceDemo(onValueChanged = { callCount++ })

        demo.value = "first"
        assertEquals(1, callCount)

        assertThrows(IllegalStateException::class.java) { demo.value = "second" }
        assertEquals(1, callCount)
    }

    @Test
    fun testOnceWithInitialValue_onValueChangedInvokedForEveryAcceptedSet() {
        var callCount = 0
        val demo = OnceDemo(initial = "init", onValueChanged = { callCount++ })

        repeat(3) { demo.value = "init" }
        assertEquals(3, callCount)

        demo.value = "changed"
        assertEquals(4, callCount)

        assertThrows(IllegalStateException::class.java) { demo.value = "changed again" }
        assertEquals(4, callCount)
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
        assertEquals("assigned", demo.value)
    }

    @Test
    fun testOnceOrNull_settingTheSameValueASecondTimeIsAlreadyRejected() {
        // Unlike once(), onceOrNull() has no initialValue sentinel to stay idempotent on - the unset
        // state is always null, and null can never be (re-)assigned. So even re-assigning the exact same
        // non-null value a second time counts as a change and is rejected.
        val demo = OnceOrNullDemo()
        demo.value = "assigned"

        assertThrows(IllegalStateException::class.java) {
            demo.value = "assigned"
        }
        assertEquals("assigned", demo.value)
    }

    @Test
    fun testOnceOrNull_secondSetSilentlyIgnoredWhenNotThrowing() {
        val demo = OnceOrNullDemo(throwOnChange = false)
        demo.value = "assigned"

        assertDoesNotThrow { demo.value = "second" }
        assertEquals("assigned", demo.value)
    }

    @Test
    fun testOnceOrNullRejectNullAssignment() {
        val demo = OnceOrNullDemo()
        assertThrows(IllegalStateException::class.java) {
            demo.value = null
        }
    }

    @Test
    fun testOnceOrNull_rejectsNullAssignmentEvenWhenNotThrowingOnChange() {
        // The null-rejection is unconditional and independent of throwOnChangeTry - it is not a "change
        // attempt" being blocked, it is an outright invalid value for this delegate.
        val demo = OnceOrNullDemo(throwOnChange = false)
        assertThrows(IllegalStateException::class.java) {
            demo.value = null
        }
        assertNull(demo.value)
    }
}
