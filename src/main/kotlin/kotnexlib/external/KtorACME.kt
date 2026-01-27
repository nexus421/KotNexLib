package kotnexlib.external

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import org.bouncycastle.asn1.DERBitString
import org.bouncycastle.asn1.pkcs.CertificationRequest
import org.bouncycastle.asn1.pkcs.CertificationRequestInfo
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers
import java.io.ByteArrayInputStream
import java.io.File
import java.net.Socket
import java.security.*
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.X509KeyManager

/**
 *
 * Warning: AI generated. Not checked or validated yet!
 * KtorACME Plugin for automatic Let's Encrypt certificate management.
 *
 * Usage:
 * ```kotlin
 * install(KtorACME.KtorACMEPlugin) {
 *     domains = listOf("example.com")
 *     email = "admin@example.com"
 *     staging = false
 * }
 *
 * // In your engine configuration (e.g. Netty):
 * embeddedServer(Netty, applicationEngineEnvironment {
 *     sslConnector(
 *         keyStore = KeyStore.getInstance(KeyStore.getDefaultType()).apply { load(null, null) },
 *         keyAlias = "main",
 *         keyStorePassword = { charArrayOf() },
 *         privateKeyPassword = { charArrayOf() }
 *     ) {
 *         port = 443
 *         keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm()).apply {
 *             init(null, null) // This is just a placeholder, the plugin handles it via getSSLContext()
 *         }
 *         // Actually, the best way is to use the SSLContext directly if the engine supports it:
 *         sslContext = KtorACME.getSSLContext()
 *     }
 *     connector { port = 80 }
 * }).start(wait = true)
 * ```
 */
@OptIn(ExperimentalSerializationApi::class)
private object KtorACME {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }

    private fun ByteArray.base64Url(): String = Base64.getUrlEncoder().withoutPadding().encodeToString(this)
    private fun String.base64Url(): String = this.toByteArray().base64Url()

    @Serializable
    data class AcmeDirectory(
        val newNonce: String,
        val newAccount: String,
        val newOrder: String,
        val revokeCert: String,
        val keyChange: String,
        val meta: AcmeMeta? = null
    )

    @Serializable
    data class AcmeMeta(
        val termsOfService: String? = null,
        val website: String? = null,
        val caaIdentities: List<String>? = null,
        val externalAccountRequired: Boolean? = null
    )

    @Serializable
    data class AcmeOrder(
        val url: String? = null,
        val status: String? = null,
        val expires: String? = null,
        val identifiers: List<AcmeIdentifier>? = null,
        val authorizations: List<String>? = null,
        val finalize: String? = null,
        val certificate: String? = null
    )

    @Serializable
    data class AcmeIdentifier(
        val type: String,
        val value: String
    )

    @Serializable
    data class AcmeAuthorization(
        val identifier: AcmeIdentifier,
        val status: String,
        val challenges: List<AcmeChallenge>,
        val expires: String? = null
    )

    @Serializable
    data class AcmeChallenge(
        val type: String,
        val url: String,
        val status: String,
        val token: String? = null,
        val error: JsonObject? = null
    )

    class ACMEClient(
        private val directoryUrl: String,
        private val accountKeyPair: KeyPair,
        private val httpClient: HttpClient
    ) {
        private var directory: AcmeDirectory? = null
        private var nonce: String? = null
        private var kid: String? = null

        private suspend fun getDirectory(): AcmeDirectory {
            if (directory == null) {
                directory = httpClient.get(directoryUrl).body()
            }
            return directory!!
        }

        private suspend fun updateNonce() {
            val response = httpClient.head(getDirectory().newNonce)
            nonce = response.headers["Replay-Nonce"]
        }

        private suspend fun getNonce(): String {
            if (nonce == null) updateNonce()
            val currentNonce = nonce!!
            nonce = null
            return currentNonce
        }

        private suspend fun jws(url: String, payload: String? = null): String {
            val protected = buildJsonObject {
                put("alg", "ES256")
                put("nonce", getNonce())
                put("url", url)
                if (kid != null) {
                    put("kid", kid)
                } else {
                    put("jwk", getJwk())
                }
            }.toString().base64Url()

            val payloadEncoded = payload?.base64Url() ?: ""
            val signature = sign(protected, payloadEncoded)

            return buildJsonObject {
                put("protected", protected)
                put("payload", payloadEncoded)
                put("signature", signature)
            }.toString()
        }

        private fun getJwk(): JsonObject {
            val publicKey = accountKeyPair.public as ECPublicKey
            val x = publicKey.w.affineX.toByteArray().toUnsignedByteArray().base64Url()
            val y = publicKey.w.affineY.toByteArray().toUnsignedByteArray().base64Url()
            return buildJsonObject {
                put("kty", "EC")
                put("crv", "P-256")
                put("x", x)
                put("y", y)
            }
        }

        private fun ByteArray.toUnsignedByteArray(): ByteArray {
            return if (this[0] == 0.toByte()) this.copyOfRange(1, this.size) else this
        }

        private fun sign(protected: String, payload: String): String {
            val data = "$protected.$payload".toByteArray()
            val signer = Signature.getInstance("SHA256withECDSA")
            signer.initSign(accountKeyPair.private)
            signer.update(data)
            val derSignature = signer.sign()
            return convertDerToRaw(derSignature).base64Url()
        }

        private fun convertDerToRaw(der: ByteArray): ByteArray {
            var offset = 2
            if (der[offset] == 0x81.toByte()) offset++
            offset++
            var lenR = der[offset++].toInt()
            var startR = offset
            if (der[startR] == 0.toByte() && lenR > 32) {
                startR++
                lenR--
            }
            val r = der.copyOfRange(startR, startR + lenR)
            offset += lenR
            offset++
            var lenS = der[offset++].toInt()
            var startS = offset
            if (der[startS] == 0.toByte() && lenS > 32) {
                startS++
                lenS--
            }
            val s = der.copyOfRange(startS, startS + lenS)
            val raw = ByteArray(64)
            System.arraycopy(r, 0, raw, 32 - r.size, r.size)
            System.arraycopy(s, 0, raw, 64 - s.size, s.size)
            return raw
        }

        suspend fun createAccount(email: String) {
            val url = getDirectory().newAccount
            val payload = buildJsonObject {
                putJsonArray("contact") { add("mailto:$email") }
                put("termsOfServiceAgreed", true)
            }.toString()
            val response = httpClient.post(url) {
                setBody(jws(url, payload))
                contentType(ContentType.Application.Json)
            }
            if (response.status == HttpStatusCode.Created || response.status == HttpStatusCode.OK) {
                kid = response.headers["Location"]
                nonce = response.headers["Replay-Nonce"]
            } else {
                throw Exception("Failed to create account: ${response.bodyAsText()}")
            }
        }

        suspend fun createOrder(domains: List<String>): AcmeOrder {
            val url = getDirectory().newOrder
            val payload = buildJsonObject {
                putJsonArray("identifiers") {
                    domains.forEach { domain ->
                        addJsonObject {
                            put("type", "dns")
                            put("value", domain)
                        }
                    }
                }
            }.toString()
            val response = httpClient.post(url) {
                setBody(jws(url, payload))
                contentType(ContentType.Application.Json)
            }
            nonce = response.headers["Replay-Nonce"]
            val orderUrl = response.headers["Location"] ?: throw Exception("Order location missing")
            return response.body<AcmeOrder>().copy(url = orderUrl)
        }

        suspend fun getOrder(orderUrl: String): AcmeOrder {
            val response = httpClient.post(orderUrl) {
                setBody(jws(orderUrl))
                contentType(ContentType.Application.Json)
            }
            nonce = response.headers["Replay-Nonce"]
            return response.body<AcmeOrder>().copy(url = orderUrl)
        }

        suspend fun getAuthorization(authUrl: String): AcmeAuthorization {
            val response = httpClient.post(authUrl) {
                setBody(jws(authUrl))
                contentType(ContentType.Application.Json)
            }
            nonce = response.headers["Replay-Nonce"]
            return response.body()
        }

        suspend fun triggerChallenge(challengeUrl: String) {
            val response = httpClient.post(challengeUrl) {
                setBody(jws(challengeUrl, "{}"))
                contentType(ContentType.Application.Json)
            }
            nonce = response.headers["Replay-Nonce"]
        }

        suspend fun finalizeOrder(finalizeUrl: String, csr: ByteArray): AcmeOrder {
            val payload = buildJsonObject {
                put("csr", csr.base64Url())
            }.toString()
            val response = httpClient.post(finalizeUrl) {
                setBody(jws(finalizeUrl, payload))
                contentType(ContentType.Application.Json)
            }
            nonce = response.headers["Replay-Nonce"]
            val orderUrl = response.headers["Location"]
            return response.body<AcmeOrder>().let { if (orderUrl != null) it.copy(url = orderUrl) else it }
        }

        suspend fun downloadCertificate(certificateUrl: String): String {
            val response = httpClient.post(certificateUrl) {
                setBody(jws(certificateUrl))
                contentType(ContentType.Application.Json)
            }
            nonce = response.headers["Replay-Nonce"]
            return response.bodyAsText()
        }

        fun getKeyAuthorization(token: String): String {
            val publicKey = accountKeyPair.public as ECPublicKey
            val x = publicKey.w.affineX.toByteArray().toUnsignedByteArray().base64Url()
            val y = publicKey.w.affineY.toByteArray().toUnsignedByteArray().base64Url()
            val jwk = buildJsonObject {
                put("crv", "P-256")
                put("kty", "EC")
                put("x", x)
                put("y", y)
            }.toString()
            val digest = MessageDigest.getInstance("SHA-256")
            val thumbprint = digest.digest(jwk.toByteArray()).base64Url()
            return "$token.$thumbprint"
        }
    }

    class DynamicKeyManager : X509KeyManager {
        private var keyManager: X509KeyManager? = null

        fun updateCertificate(keyPair: KeyPair, chain: List<X509Certificate>) {
            val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
            keyStore.load(null, null)
            keyStore.setKeyEntry("main", keyPair.private, charArrayOf(), chain.toTypedArray())
            val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
            kmf.init(keyStore, charArrayOf())
            keyManager = kmf.keyManagers.filterIsInstance<X509KeyManager>().first()
        }

        override fun getClientAliases(keyType: String?, issuers: Array<out Principal>?): Array<String>? =
            keyManager?.getClientAliases(keyType, issuers)

        override fun chooseClientAlias(
            keyType: Array<out String>?,
            issuers: Array<out Principal>?,
            socket: Socket?
        ): String? = keyManager?.chooseClientAlias(keyType, issuers, socket)

        override fun getServerAliases(keyType: String?, issuers: Array<out Principal>?): Array<String>? =
            keyManager?.getServerAliases(keyType, issuers)

        override fun chooseServerAlias(keyType: String?, issuers: Array<out Principal>?, socket: Socket?): String? =
            keyManager?.chooseServerAlias(keyType, issuers, socket)

        override fun getCertificateChain(alias: String?): Array<X509Certificate>? =
            keyManager?.getCertificateChain(alias)

        override fun getPrivateKey(alias: String?): PrivateKey? = keyManager?.getPrivateKey(alias)
    }

    class KtorACMEConfig {
        var domains: List<String> = emptyList()
        var email: String = ""
        var staging: Boolean = true
        var certFolder: File = File("certs")
        var acmeClientCustom: ACMEClient? = null
    }

    fun generateCSR(domain: String, keyPair: KeyPair): ByteArray {
        val x500Name = X500Name("CN=$domain")
        val subjectPublicKeyInfo = SubjectPublicKeyInfo.getInstance(keyPair.public.encoded)
        val requestInfo = CertificationRequestInfo(x500Name, subjectPublicKeyInfo, null)
        val signer = Signature.getInstance("SHA256withECDSA")
        signer.initSign(keyPair.private)
        signer.update(requestInfo.encoded)
        val signature = signer.sign()
        val ecdsaWithSha256 = AlgorithmIdentifier(X9ObjectIdentifiers.ecdsa_with_SHA256)
        val certificationRequest = CertificationRequest(requestInfo, ecdsaWithSha256, DERBitString(signature))
        return certificationRequest.encoded
    }

    private val dynamicKeyManager = DynamicKeyManager()

    fun getSSLContext(): SSLContext {
        val context = SSLContext.getInstance("TLS")
        context.init(arrayOf(dynamicKeyManager), null, SecureRandom())
        return context
    }

    val KtorACMEPlugin = createApplicationPlugin(name = "KtorACME", createConfiguration = ::KtorACMEConfig) {
        val domains = pluginConfig.domains
        val email = pluginConfig.email
        val staging = pluginConfig.staging
        val certFolder = pluginConfig.certFolder
        if (domains.isEmpty()) return@createApplicationPlugin
        if (!certFolder.exists()) certFolder.mkdirs()
        val accountKeyFile = File(certFolder, "account.key")
        val accountPubFile = File(certFolder, "account.pub")
        val domainKeyFile = File(certFolder, "domain.key")
        val domainPubFile = File(certFolder, "domain.pub")
        val certFile = File(certFolder, "domain.crt")

        fun loadOrCreateKeyPair(keyFile: File, pubFile: File): KeyPair {
            return if (keyFile.exists() && pubFile.exists()) {
                val kf = KeyFactory.getInstance("EC")
                val priv = kf.generatePrivate(PKCS8EncodedKeySpec(keyFile.readBytes()))
                val pub = kf.generatePublic(X509EncodedKeySpec(pubFile.readBytes()))
                KeyPair(pub, priv)
            } else {
                val g = KeyPairGenerator.getInstance("EC")
                g.initialize(ECGenParameterSpec("P-256"))
                val kp = g.generateKeyPair()
                keyFile.writeBytes(kp.private.encoded)
                pubFile.writeBytes(kp.public.encoded)
                kp
            }
        }

        val accountKeyPair = loadOrCreateKeyPair(accountKeyFile, accountPubFile)
        val domainKeyPair = loadOrCreateKeyPair(domainKeyFile, domainPubFile)
        val acmeUrl =
            if (staging) "https://acme-staging-v02.api.letsencrypt.org/directory" else "https://acme-v02.api.letsencrypt.org/directory"
        val httpClient = HttpClient {
            install(ContentNegotiation) { json(json) }
        }
        val acmeClient = pluginConfig.acmeClientCustom ?: ACMEClient(acmeUrl, accountKeyPair, httpClient)
        var currentChallengeProvider: ((String) -> String?)? = null

        application.routing {
            get("/.well-known/acme-challenge/{token}") {
                val token = call.parameters["token"] ?: return@get
                val response = currentChallengeProvider?.invoke(token)
                if (response != null) {
                    call.respondText(response)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
        }

        fun loadCert(): List<X509Certificate>? {
            if (!certFile.exists()) return null
            val cf = CertificateFactory.getInstance("X.509")
            val certs = cf.generateCertificates(ByteArrayInputStream(certFile.readBytes()))
            return certs.map { it as X509Certificate }
        }

        suspend fun refreshCertificate() {
            try {
                acmeClient.createAccount(email)
            } catch (e: Exception) {
            }
            val order = acmeClient.createOrder(domains)
            for (authUrl in order.authorizations!!) {
                val auth = acmeClient.getAuthorization(authUrl)
                if (auth.status == "valid") continue
                val challenge = auth.challenges.find { it.type == "http-01" } ?: continue
                val response = acmeClient.getKeyAuthorization(challenge.token!!)
                currentChallengeProvider = { token -> if (token == challenge.token) response else null }
                acmeClient.triggerChallenge(challenge.url)
                var status = "pending"
                while (status == "pending" || status == "processing") {
                    delay(2000)
                    status = acmeClient.getAuthorization(authUrl).status
                }
                if (status != "valid") throw Exception("Challenge failed with status $status")
            }
            val csr = generateCSR(domains.first(), domainKeyPair)
            var finalizedOrder = acmeClient.finalizeOrder(order.finalize!!, csr)
            val orderUrl = order.url ?: throw Exception("Order URL missing")
            while (finalizedOrder.status == "pending" || finalizedOrder.status == "processing") {
                delay(2000)
                finalizedOrder = acmeClient.getOrder(orderUrl)
                if (finalizedOrder.status == "invalid") throw Exception("Order became invalid")
            }
            if (finalizedOrder.status == "valid") {
                val certChainPem = acmeClient.downloadCertificate(finalizedOrder.certificate!!)
                certFile.writeText(certChainPem)
                val chain = loadCert()!!
                dynamicKeyManager.updateCertificate(domainKeyPair, chain)
            }
            currentChallengeProvider = null
        }

        val existingChain = loadCert()
        if (existingChain != null) {
            val now = Date()
            val expiry = existingChain.first().notAfter
            val daysBeforeExpiry = (expiry.time - now.time) / (1000 * 60 * 60 * 24)
            if (daysBeforeExpiry < 30) {
                application.launch {
                    try {
                        refreshCertificate()
                    } catch (e: Exception) {
                        application.environment.log.error("ACME refresh failed", e)
                    }
                }
            } else {
                dynamicKeyManager.updateCertificate(domainKeyPair, existingChain)
            }
        } else {
            application.launch {
                try {
                    refreshCertificate()
                } catch (e: Exception) {
                    application.environment.log.error("ACME refresh failed", e)
                }
            }
        }

        application.launch {
            while (isActive) {
                delay(24 * 60 * 60 * 1000)
                val chain = loadCert()
                if (chain == null) {
                    try {
                        refreshCertificate()
                    } catch (e: Exception) {
                        application.environment.log.error("ACME refresh failed", e)
                    }
                } else {
                    val now = Date()
                    val expiry = chain.first().notAfter
                    val daysBeforeExpiry = (expiry.time - now.time) / (1000 * 60 * 60 * 24)
                    if (daysBeforeExpiry < 30) {
                        try {
                            refreshCertificate()
                        } catch (e: Exception) {
                            application.environment.log.error("ACME refresh failed", e)
                        }
                    }
                }
            }
        }
    }
}

