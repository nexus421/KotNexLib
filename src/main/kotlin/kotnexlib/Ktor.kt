package kotnexlib

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*

object Ktor {
    /**
     * Represents an API key with associated metadata.
     * All path comparisons performed using this key's `path` property are **case-sensitive**.
     *
     * @property key The string value of the API key. This comparison is also case-sensitive.
     * @property name A descriptive name associated with the API key.
     * @property path Optional path for which this API key is valid.
     * If `null`, the key grants access to all paths.
     * If set, access is determined by the [pathValidationType] and is **case-sensitive**.
     * @property pathValidationType The type of validation to apply if [path] is not null. Defaults to [PathValidationType.EXACT].
     */
    class ApiKey(
        val key: String,
        val name: String,
        path: String? = null,
        val pathValidationType: PathValidationType = PathValidationType.EXACT
    ) {
        val path = path?.normalizeConfiguredPath()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ApiKey

            if (key != other.key) return false
            if (name != other.name) return false
            if (pathValidationType != other.pathValidationType) return false
            if (path != other.path) return false

            return true
        }

        override fun hashCode(): Int {
            var result = key.hashCode()
            result = 31 * result + name.hashCode()
            result = 31 * result + pathValidationType.hashCode()
            result = 31 * result + (path?.hashCode() ?: 0)
            return result
        }

        override fun toString() = "ApiKey(name='$name', path=${this.path ?: "ANY"}, validationType=$pathValidationType)"

    }

    /**
     * Represents a server path that should be ignored for API key checks.
     * All path comparisons are **case-sensitive**.
     *
     * @property path The path to be ignored. The comparison against the request path is **case-sensitive**.
     * @property pathValidationType Defines how the [path] should be matched against the request path. Defaults to [PathValidationType.EXACT].
     */
    class IgnorePath(path: String, val pathValidationType: PathValidationType = PathValidationType.EXACT) {
        val path = path.normalizeConfiguredPath()
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as IgnorePath

            if (pathValidationType != other.pathValidationType) return false
            if (path != other.path) return false

            return true
        }

        override fun hashCode(): Int {
            var result = pathValidationType.hashCode()
            result = 31 * result + path.hashCode()
            return result
        }

        override fun toString() = "IgnorePath(path='${this.path}', validationType=$pathValidationType)"
    }

    /**
     * Specifies the type of validation applied to a path.
     * All path comparisons are **case-sensitive**.
     *
     * - `EXACT`: Requires the request path to exactly match the configured path (case-sensitively).
     * - `STARTS_WITH`: Requires the request path to start with the configured path prefix (case-sensitively).
     */
    enum class PathValidationType {
        EXACT,
        STARTS_WITH
    }

}

private fun String.normalizeConfiguredPath(): String = if (isBlank() || this == "/") "/" else removeSuffix("/")

/**
 * Ktor application feature to intercept requests and validate API keys.
 *
 * This middleware checks incoming requests for an API key specified in a header.
 * It validates the key against a provided list of [allowedApiKeys].
 * Access can be further restricted based on path permissions associated with each API key.
 * Certain paths can be exempted from this validation if listed in [ignorePaths].
 *
 * **Important Notes on Path Handling:**
 * - All path comparisons (for both `ignorePaths` and `ApiKey.path` restrictions) are **case-sensitive**.
 * - Incoming request paths are normalized:
 * - An empty or blank path (e.g., from `call.request.path()`) is treated as "/".
 * - Trailing slashes are removed from non-root paths (e.g., "/users/" becomes "/users"). The root path "/" remains "/".
 *
 * Unauthorized or forbidden requests will receive appropriate HTTP status codes (500 for server misconfiguration,
 * 401 for missing/invalid API key, 403 if API key is valid but not authorized for the specific path).
 *
 * @param allowedApiKeys A list of [Ktor.ApiKey] objects. If this list is empty, the server will respond with
 * [HttpStatusCode.InternalServerError] to all requests handled by this interceptor.
 * @param ignorePaths A list of [Ktor.IgnorePath] objects. Requests to these paths (matched case-sensitively
 * according to their [Ktor.PathValidationType]) will bypass API key validation.
 * Defaults to a single rule ignoring the exact root path "/".
 * @param apiKeyParameterName The name of the HTTP request header that is expected to contain the API key.
 * Defaults to "API-KEY".
 */
fun Application.checkApiKey(
    allowedApiKeys: List<Ktor.ApiKey>,
    ignorePaths: List<Ktor.IgnorePath> = listOf(Ktor.IgnorePath("/")),
    apiKeyParameterName: String = "API-KEY"
) {
    intercept(ApplicationCallPipeline.Setup) { _ ->
        val logger = call.application.environment.log

        if (allowedApiKeys.isEmpty()) {
            logger.error("API Key Middleware Misconfiguration: No allowed API keys are defined. Denying all requests.")
            call.respond(HttpStatusCode.InternalServerError, "No allowed API-Keys provided!")
            finish()
            return@intercept
        }
        //Alle Pfade, die einen der ignorePaths beinhalten, werden ignoriert.
        val calledPath = call.request.path().normalizeConfiguredPath()
        ignorePaths.forEach { ignorePath ->
            val shouldIgnoreThisPath =
                if (ignorePath.pathValidationType == Ktor.PathValidationType.EXACT) calledPath == ignorePath.path else calledPath.startsWith(
                    ignorePath.path
                )
            if (shouldIgnoreThisPath) return@intercept
        }

        val receivedApiKeyFromHeader = call.request.headers[apiKeyParameterName]

        if (receivedApiKeyFromHeader == null) {
            logger.warn("API Key Check: Unauthorized - API Key missing in header '$apiKeyParameterName' for path '$calledPath'.")
            call.respond(HttpStatusCode.Unauthorized, "API Key required.")
            finish()
            return@intercept
        }

        //Suchen des API-Keys aus dem Header in den erlaubten API-Keys
        val foundApiKey = allowedApiKeys.find { it.key == receivedApiKeyFromHeader }

        //Der angegebene API-Key existiert nicht in unserer Liste. Ist also immer ungültig.
        if (foundApiKey == null) {
            logger.warn(
                "API Key Check: Unauthorized - Invalid API Key provided ('${
                    receivedApiKeyFromHeader.coverString(
                        5,
                        20
                    )
                }') for path '$calledPath'."
            )
            call.respond(HttpStatusCode.Unauthorized)
            finish()
            return@intercept
        }

        if (foundApiKey.path == null) {
            logger.info("API Key Check: Authorized - API Key '${foundApiKey.name}' grants global access. Path '$calledPath' allowed.")
            return@intercept // Der API-Key hat auf alles Zugriff.
        }

        val pathAccessAllowed = when (foundApiKey.pathValidationType) {
            Ktor.PathValidationType.EXACT -> foundApiKey.path == calledPath
            Ktor.PathValidationType.STARTS_WITH -> calledPath.startsWith(foundApiKey.path)
        }

        if (pathAccessAllowed.not()) { //Der API-Key ist für den verwendeten Pfad nicht zulässig.
            logger.warn("API Key Check: Forbidden - API Key '${foundApiKey.name}' (rule: ${foundApiKey.pathValidationType} '${foundApiKey.path}') does NOT allow access to path '$calledPath'.")
            call.respond(HttpStatusCode.Forbidden)
            finish()
            return@intercept
        }
    }
}