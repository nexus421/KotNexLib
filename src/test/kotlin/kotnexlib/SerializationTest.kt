package kotnexlib

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.Serializable

class SerializationTest {

    data class Person(val name: String, val age: Int) : Serializable

    @Test
    fun testSerializationRoundtrip() {
        val person = Person("Alice", 30)
        val serializedString = person.serializeToString()
        assertTrue(serializedString.isNotEmpty())

        val deserialized: Person? = serializedString.deserializeFromStringOrNull()
        assertEquals(person, deserialized)

        val resultSuccess: Result<Person> = serializedString.deserializeFromString()
        assertTrue(resultSuccess.isSuccess)
        assertEquals(person, resultSuccess.getOrNull())
    }

    @Test
    fun testSerializationErrorHandling() {
        var errorReported: Throwable? = null
        val invalidBase64 = "ThisIsNotValidBase64OrSerializedObject"

        val deserialized: Person? = invalidBase64.deserializeFromStringOrNull(onError = {
            errorReported = it
        })

        assertNull(deserialized)
        assertNotNull(errorReported)

        val resultFail: Result<Person> = invalidBase64.deserializeFromString()
        assertTrue(resultFail.isFailure)
    }
}
