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

}