package kotnexlib

import kotnexlib.security.TOTP
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TOTPTest {

    @Test
    fun testGenerateSecret() {
        val secret = TOTP.Registration.generateSecret(32)
        assertEquals(32, secret.length)
        assertTrue(secret.all { it in "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567" })
    }

    @Test
    fun testClientAndServerVerification() {
        val secret = TOTP.Registration.generateSecret()
        val now = System.currentTimeMillis()

        val clientCode = TOTP.Verification.clientGenerateCode(secret, timeMillis = now)
        assertEquals(6, clientCode.length)
        assertTrue(clientCode.all { it.isDigit() })

        val isValid = TOTP.Verification.serverVerifyCode(secret, clientCode, window = 1, timeMillis = now)
        assertTrue(isValid)

        // Invalid code should fail
        val isInvalid = TOTP.Verification.serverVerifyCode(secret, "999999", window = 1, timeMillis = now)
        assertFalse(isInvalid)
    }

    @Test
    fun testVerificationTimeWindow() {
        val secret = TOTP.Registration.generateSecret()
        val baseTime = 1700000000000L // arbitrary fixed timestamp

        // Code generated 25 seconds ago (within window = 1 of 30-sec step)
        val codePast = TOTP.Verification.clientGenerateCode(secret, timeMillis = baseTime - 25000L)
        assertTrue(TOTP.Verification.serverVerifyCode(secret, codePast, window = 1, timeMillis = baseTime))

        // Code generated 120 seconds ago (outside window = 1)
        val codeFarPast = TOTP.Verification.clientGenerateCode(secret, timeMillis = baseTime - 120000L)
        assertFalse(TOTP.Verification.serverVerifyCode(secret, codeFarPast, window = 1, timeMillis = baseTime))
    }
}
