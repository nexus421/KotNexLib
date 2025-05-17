package kotnexlib

import java.math.BigInteger

object IBAN {

    private val mod97 = 97.toBigInteger()

    /**
     * Validates the input International Bank Account Number (IBAN).
     *
     * @param iban The IBAN to be validated.
     * @return `true` if the IBAN is valid, `false` otherwise.
     */
    fun isValidIban(iban: String): Boolean {
        val clearedIban = iban.replace(" ", "")
        if (clearedIban.length < 15 || clearedIban.length > 34) return false

        val rearranged = clearedIban.substring(4) + clearedIban.substring(0, 4)
        val digits = rearranged.map { if (it.isDigit()) it.toString() else (it.code - 55).toString() }.joinToString("")

        return BigInteger(digits).mod(mod97).toInt() == 1
    }

    /**
     * Formats an IBAN by inserting spaces every 4 characters for better readability.
     *
     * @param iban The IBAN to be formatted.
     * @return The formatted IBAN with spaces every 4 characters.
     */
    fun formatIban(iban: String): String {
        val clearedIban = iban.replace(" ", "")
        return clearedIban.chunked(4).joinToString(" ")
    }

    /**
     * Extracts the country code from an IBAN.
     *
     * @param iban The IBAN to extract the country code from.
     * @return The two-letter country code or null if the IBAN is too short.
     */
    fun getCountryCode(iban: String): String? {
        val clearedIban = iban.replace(" ", "")
        return if (clearedIban.length >= 2) clearedIban.substring(0, 2) else null
    }

    /**
     * Extracts the check digits from an IBAN.
     *
     * @param iban The IBAN to extract the check digits from.
     * @return The two-digit check number or null if the IBAN is too short.
     */
    fun getCheckDigits(iban: String): String? {
        val clearedIban = iban.replace(" ", "")
        return if (clearedIban.length >= 4) clearedIban.substring(2, 4) else null
    }
}

/**
 * Extension function to validate an IBAN.
 *
 * @return `true` if this string is a valid IBAN, `false` otherwise.
 */
fun String.isValidIban(): Boolean = IBAN.isValidIban(this)
