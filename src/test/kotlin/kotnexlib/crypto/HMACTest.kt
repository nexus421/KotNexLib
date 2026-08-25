package kotnexlib.crypto

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class HMACTest {

    // Test vectors cross-checked against Python hmac/hashlib and the OpenSSL CLI.
    @Test
    fun testKnownVectorSha256() {
        assertEquals(
            "734cc62f32841568f45715aeb9f4d7891324e6d948e4c6c60c0621cdac48623a",
            HMAC.computeHex("hello world", "secret", HMAC.Algorithm.SHA256)
        )
    }

    @Test
    fun testKnownVectorSha512() {
        assertEquals(
            "6d32239b01dd1750557211629313d95e4f4fcb8ee517e443990ac1afc7562bfd74ffa6118387efd9e168ff86d1da5cef4a55edc63cc4ba289c4c3a8b4f7bdfc2",
            HMAC.computeHex("hello world", "secret", HMAC.Algorithm.SHA512)
        )
    }

    @Test
    fun testStringExtensionMatchesDirectCall() {
        assertEquals(HMAC.computeHex("payload", "key"), "payload".hmacSha256("key"))
    }

    @Test
    fun testDifferentKeyOrDataChangesSignature() {
        val base = HMAC.computeHex("data", "key")
        assertFalse(base == HMAC.computeHex("data", "otherKey"))
        assertFalse(base == HMAC.computeHex("otherData", "key"))
    }

    @Test
    fun testVerifyConstantTime() {
        val sig = HMAC.compute("data".toByteArray(), "key".toByteArray())
        val sameSig = HMAC.compute("data".toByteArray(), "key".toByteArray())
        val otherSig = HMAC.compute("data".toByteArray(), "key2".toByteArray())

        assertTrue(HMAC.verifyConstantTime(sig, sameSig))
        assertFalse(HMAC.verifyConstantTime(sig, otherSig))
    }

    @Test
    fun testEmptyKeyIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            HMAC.compute("data".toByteArray(), ByteArray(0))
        }
    }
}
