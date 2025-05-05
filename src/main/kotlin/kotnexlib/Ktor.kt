package kotnexlib

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*

object Ktor {
    /**
     * Represents an API key with associated metadata such as its name and optional path.
     *
     * @property key The string value of the API key.
     * @property name Descriptive name associated with the API key.
     * @property path Optional path information related to the API key. Null == Will be checked on all paths. Otherwise only on the exact given path.
     */
    data class ApiKey(val key: String, val name: String, val path: String? = null)

    /**
     * Represents a server path that should be ignored for an API-Key-Check.
     *
     * @property path The path to be ignored, represented as a string.
     * @property equals Defines whether the path should be matched exactly (`true` for exact match, `false` for startsWith match).
     */
    data class IgnorePath(val path: String, val equals: Boolean = false)

}

/**
 * Middleware to check the validity of an API key for incoming application requests.
 *
 * @param allowedApiKeys A list of `ApiKey` objects representing the allowed API keys for the application.
 * @param ignorePaths A list of `IgnorePath` objects representing paths to be excluded from API key validation.
 *                     Defaults to a single ignore path of `/`.
 * @param apiKeyParameterName The name of the parameter in the HTTP headers that contains the API key.
 *                            Defaults to "API-KEY".
 */
fun Application.checkApiKey(
    allowedApiKeys: List<Ktor.ApiKey>,
    ignorePaths: List<Ktor.IgnorePath> = listOf(Ktor.IgnorePath("/")),
    apiKeyParameterName: String = "API-KEY"
) {
    intercept(ApplicationCallPipeline.Setup) { _ ->
        if (allowedApiKeys.isEmpty()) return@intercept call.respond(
            HttpStatusCode.InternalServerError,
            "No allowed API-Keys provided!"
        )
        //Alle Pfade, die einen der ignorePaths beinhalten, werden ignoriert.
        val calledPath = call.request.path().lowercase().let { path -> if (path == "") "/" else path }
        ignorePaths.forEach { ignorePath ->
            val shouldIgnoreThisPath =
                if (ignorePath.equals) calledPath == ignorePath.path.lowercase() else calledPath.startsWith(ignorePath.path.lowercase())
            if (shouldIgnoreThisPath) return@intercept
        }

        val apiKey = call.request.headers[apiKeyParameterName]

        //Suchen des API-Keys aus dem Header in den erlaubten API-Keys
        val foundApiKey = allowedApiKeys.find { it.key == apiKey }

        //Der angegebene API-Key existiert nicht in unserer Liste. Ist also immer ungültig.
        if (foundApiKey == null) return@intercept call.respond(HttpStatusCode.Unauthorized)

        if (foundApiKey.path == null) return@intercept //Der API-Key hat auf alles Zugriff. Passt und weiter.
        else if (foundApiKey.path == calledPath) return@intercept //Der API-Key ist für exakt diesen Pfad freigegeben. Passt und weiter.
        else return@intercept call.respond(HttpStatusCode.Unauthorized) //Der API-Key ist für den verwendeten Pfad nicht zulässig.
    }
}