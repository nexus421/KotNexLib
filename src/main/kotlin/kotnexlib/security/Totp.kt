package kotnexlib.security

import java.nio.ByteBuffer
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * A simple implementation of Time-based One-Time Password (TOTP) according to RFC 6238.
 * This object provides methods to generate secrets for password managers and to verify TOTP codes.
 *
 * ### Usage Flow:
 * 1. **Registration**:
 *    - The **Server** calls [Registration.generateSecret] to create a new shared secret.
 *    - The **Server** stores this secret securely in its database (associated with the user).
 *    - The **Server** provides the secret to the **User** (e.g., as a string or QR code).
 *    - The **User** enters the secret into their TOTP application (e.g., 1Password, Bitwarden, Google Authenticator).
 *
 * 2. **Verification**:
 *    - The **User's Password Manager** generates a 6-digit code based on the shared secret and the current time. (optional with [Verification.clientGenerateCode])
 *    - The **User** provides this code to the **Server**.
 *    - The **Server** retrieves the stored secret and calls [Verification.serverVerifyCode] with the user-provided code.
 *
 * No external libraries are used for the TOTP logic itself.
 */
object TOTP {

    private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
    private val RANDOM = SecureRandom()

    object Registration {

        /**
         * Generates a new secret key in Base32 format.
         * This string can be stored in password managers (like 1Password, Bitwarden, etc.).
         *
         * @param length The number of characters for the secret. Default is 32 (160 bits of entropy).
         * @return A Base32 encoded secret string.
         */
        fun generateSecret(length: Int = 32): String {
            val bytes = ByteArray((length * 5 + 7) / 8)
            RANDOM.nextBytes(bytes)
            return encodeBase32(bytes).take(length)
        }
    }

    object Verification {

        /**
         * Verifies a TOTP code against a given secret. (Server)
         *
         * @param secret The Base32 encoded secret key.
         * @param code The 6-digit code to verify (e.g., "123456").
         * @param window The number of 30-second windows to check before and after the current time.
         *               Default is 1, meaning it checks the previous, current, and next interval.
         * @param timeMillis The reference time in milliseconds. Default is the current system time.
         * @return True if the code matches any of the calculated codes in the time window.
         */
        fun serverVerifyCode(
            secret: String,
            code: String,
            window: Int = 1,
            timeMillis: Long = System.currentTimeMillis()
        ): Boolean {
            val currentInterval = timeMillis / 30000L
            for (i in -window..window) {
                if (generateCodeAtInterval(secret, currentInterval + i) == code) return true
            }
            return false
        }

        /**
         * Generates the current 6-digit TOTP code for the given secret. (Client)
         *
         * @param secret The Base32 encoded secret key.
         * @param timeMillis The reference time in milliseconds. Default is current time.
         * @return A 6-digit TOTP code as a String.
         */
        fun clientGenerateCode(secret: String, timeMillis: Long = System.currentTimeMillis()): String {
            return generateCodeAtInterval(secret, timeMillis / 30000L)
        }
    }

    /**
     * Internal method to generate a TOTP code for a specific 30-second interval.
     */
    private fun generateCodeAtInterval(secret: String, interval: Long): String {
        val key = decodeBase32(secret)
        val data = ByteBuffer.allocate(8).putLong(interval).array()

        val mac = Mac.getInstance("HmacSHA1")
        mac.init(SecretKeySpec(key, "HmacSHA1"))
        val hash = mac.doFinal(data)

        // Dynamic truncation as per RFC 4226
        val offset = (hash.last().toInt() and 0x0F)
        val truncatedHash = ByteBuffer.wrap(hash, offset, 4).int and 0x7FFFFFFF
        val otp = truncatedHash % 1000000

        return "%06d".format(otp)
    }

    /**
     * Minimal Base32 encoding (RFC 4648).
     */
    private fun encodeBase32(bytes: ByteArray): String {
        val sb = StringBuilder()
        var buffer = 0
        var bitsLeft = 0
        bytes.forEach { b ->
            buffer = (buffer shl 8) or (b.toInt() and 0xFF)
            bitsLeft += 8
            while (bitsLeft >= 5) {
                sb.append(ALPHABET[(buffer shr (bitsLeft - 5)) and 0x1F])
                bitsLeft -= 5
            }
        }
        if (bitsLeft > 0) sb.append(ALPHABET[(buffer shl (5 - bitsLeft)) and 0x1F])

        return sb.toString()
    }

    /**
     * Minimal Base32 decoding (RFC 4648).
     */
    private fun decodeBase32(base32: String): ByteArray {
        val cleaned = base32.uppercase().filter { it in ALPHABET }
        val out = ByteArray((cleaned.length * 5) / 8)
        var buffer = 0
        var bitsLeft = 0
        var index = 0
        cleaned.forEach { c ->
            buffer = (buffer shl 5) or ALPHABET.indexOf(c)
            bitsLeft += 5
            if (bitsLeft >= 8) {
                out[index++] = (buffer shr (bitsLeft - 8)).toByte()
                bitsLeft -= 8
            }
        }
        return out
    }
}
