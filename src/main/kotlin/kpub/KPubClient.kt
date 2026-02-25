package kpub

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable

/**
 * Client for the KPub service to send emails and SMS.
 *
 * @param serverUrl The URL of the KPub server.
 * @param token The authentication token.
 * @param port The port of the KPub server (optional).
 */
class KPubClient(
    private val serverUrl: String,
    private val token: String,
    private val port: Int? = null
) : AutoCloseable {

    private val client: HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    private val baseUrl: String by lazy {
        val base = if (serverUrl.startsWith("http")) serverUrl else "https://$serverUrl"
        if (port != null) "$base:$port" else base
    }

    /**
     * Sends a request to the KPub server.
     *
     * @param content The message content.
     * @param type The type of message to send (Mail, SMS, Both).
     * @param title The optional title (for emails only).
     * @return The [HttpResponse] from the server.
     */
    suspend fun send(content: String, type: SendType = SendType.Mail, title: String? = null): HttpResponse {
        return client.post("$baseUrl/send") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(SendRequest(type, content, title))
        }
    }

    override fun close() {
        client.close()
    }

    /**
     * Message type to send. "Both" sends Mail and SMS.
     */
    @Serializable
    enum class SendType { Mail, SMS, Both }

    /**
     * Payload for message sending. Defaults to sending an email when no type is provided.
     */
    @Serializable
    data class SendRequest(
        val type: SendType = SendType.Mail,
        val content: String,
        val title: String? = null,
    )
}
