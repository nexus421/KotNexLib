package kotnexlib

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