package kotnexlib

import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

//Vielleicht hiermit: https://medium.com/@sahar.asadian90/cryptography-692b323cb7b5

//import com.google.crypto.tink.Aead
//import com.google.crypto.tink.InsecureSecretKeyAccess
//import com.google.crypto.tink.TinkJsonProtoKeysetFormat
//import com.google.crypto.tink.aead.AeadConfig
//
//fun encryptData(data: ByteArray, password: String): ByteArray {
//    AeadConfig.register()
//    val aead = TinkJsonProtoKeysetFormat.parseKeyset(password, InsecureSecretKeyAccess.get()).getPrimitive(Aead::class.java)
//    return aead.encrypt(data, null)
//}
//
//fun decryptData(data: ByteArray, password: String): ByteArray {
//    AeadConfig.register()
//    val aead = TinkJsonProtoKeysetFormat.parseKeyset(password, InsecureSecretKeyAccess.get()).getPrimitive(Aead::class.java)
//    return aead.decrypt(data, null)
//}
//
//fun main() {
//    try {
//        println(String(encryptData("Bananarama".toByteArray(), "Kadoffe")))
//    } catch(e: Exception) {
//        println(e.message)
//    }
//}


/**
 * Simple method to encrypt a String with the Blowfish algorithm.
 *
 * Decompress with [decryptWithBlowfish]
 *
 * WARNING: This is not as secure as AES/CBC/PKCS5Padding because the key is only 64 bit long. Whatever: This is still
 * a secure algorithm.
 *
 * @param textToEncrypt will be decrypted with the given password.
 * @param password will be used for encryption (and decryption!).
 * @param compress if set to true the String will be compressed first and then encrypted. That could minimize the final String size.
 *
 * @return the final encrypted String as Base64 or null on any error.
 */
fun encryptWithBlowfish(textToEncrypt: String, password: String, compress: Boolean = false): String? =
    tryOrNull(onError = {
        it.printStackTrace()
    }) {
        val keyData: ByteArray = password.toByteArray()
        val secretKeySpec = SecretKeySpec(keyData, "Blowfish")
        val cipher = Cipher.getInstance("Blowfish").apply {
            init(Cipher.ENCRYPT_MODE, secretKeySpec)
        }

        val encryptedBytes = cipher.doFinal((if (compress) textToEncrypt.compress() else textToEncrypt)?.toByteArray())
        return encryptedBytes.toBase64()
    }

/**
 * Simple method to decrypt a String with the Blowfish algorithm.
 *
 * @param encryptedText the text which was encrypted with blowfish. Example: [encryptWithBlowfish].
 * @param password the password to decrypt the encrypted String.
 * @param compressed set this to true if you used [encryptWithBlowfish] and set the compress flag there on true!
 *
 * @return the decrypted String or null on any error.
 */
fun decryptWithBlowfish(encryptedText: String, password: String, compressed: Boolean = false): String? =
    tryOrNull(onError = {
        it.printStackTrace()
    }) {
        val keyData: ByteArray = password.toByteArray()
        val secretKeySpec = SecretKeySpec(keyData, "Blowfish")
        val cipher = Cipher.getInstance("Blowfish").apply {
            init(Cipher.DECRYPT_MODE, secretKeySpec)
        }

        val encrypted = String(cipher.doFinal(encryptedText.fromBase64ToByteArray()), Charsets.UTF_8)

        return if (compressed) encrypted.decompress() else encrypted
    }