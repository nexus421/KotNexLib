package kotnexlib

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class IBANTest {

    @Test
    fun testValidIban() {
        // Example German IBAN (fictional but valid format)
        val validIban = "DE89 3704 0044 0532 0130 00"
        assertTrue(IBAN.isValidIban(validIban))
        assertTrue(validIban.isValidIban())
    }

    @Test
    fun testInvalidIban() {
        val invalidIban = "DE89 3704 0044 0532 0130 01" // Changed last digit
        assertFalse(IBAN.isValidIban(invalidIban))

        assertFalse(IBAN.isValidIban("SHORT"))
        assertFalse(IBAN.isValidIban("This is definitely not an IBAN and is also too long for one"))
    }

    @Test
    fun testFormatIban() {
        val rawIban = "DE89370400440532013000"
        val expected = "DE89 3704 0044 0532 0130 00"
        assertEquals(expected, IBAN.formatIban(rawIban))
    }

    @Test
    fun testGetCountryCode() {
        assertEquals("DE", IBAN.getCountryCode("DE89 3704 0044 0532 0130 00"))
        assertNull(IBAN.getCountryCode("D"))
    }

    @Test
    fun testGetCheckDigits() {
        assertEquals("89", IBAN.getCheckDigits("DE89 3704 0044 0532 0130 00"))
        assertNull(IBAN.getCheckDigits("DE8"))
    }
}
