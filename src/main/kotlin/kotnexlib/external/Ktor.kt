package kotnexlib.external

import file.isZipFile
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotnexlib.coverString
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.concurrent.thread
import kotlin.math.abs

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

/**
 * Ktor application feature to enable server self-update functionality.
 *
 * This extension function creates an endpoint that allows the server to update itself by receiving a JAR file,
 * creating a backup of the current JAR, replacing it with the new one, and optionally restarting the server.
 *
 * **Security Considerations:**
 * - The endpoint is protected by a password that must be provided as a query parameter.
 * - The uploaded file is validated to ensure it's a valid JAR file.
 * - The server creates a backup of the current JAR before replacing it.
 * - Only one file can be uploaded at a time.
 * - If a restart script path is provided, the function validates that the script exists and is executable.
 * - The uploaded file size is checked against the current file size to ensure it doesn't deviate too much.
 *
 * **Usage Example:**
 * ```kotlin
 * fun Application.configureServer() {
 *     // Configure server self-update
 *     serverSelfUpdate(
 *         password = "your-secure-password",
 *         serverJarPath = "/path/to/your/server.jar",
 *         restartScriptPath = "/path/to/restart-script.sh"
 *     )
 * }
 * ```
 *
 * @param password The password required to authorize the update operation.
 * @param serverJarPath The full path to the current server JAR file that will be updated.
 * @param restartScriptPath The path to a script that will be executed to restart the server after the update. If null, the server will not be automatically restarted.
 * @param updateEndpoint The endpoint path for the update functionality. Defaults to "/publishUpdateToServer".
 * @param passwordParam The name of the query parameter for the password. Defaults to "password".
 * @param maxFileSize The maximum allowed size for the uploaded JAR file in bytes. Defaults to 100MB.
 * @param maxFileSizeDeviation The maximum allowed percentage deviation in file size between the current and uploaded JAR files. Defaults to 10%.
 */
fun Application.serverSelfUpdate(
    password: String,
    serverJarPath: String,
    restartScriptPath: String? = null,
    updateEndpoint: String = "/publishUpdateToServer",
    passwordParam: String = "password",
    maxFileSize: Long = 100 * 1024 * 1024, // Default: 100MB
    maxFileSizeDeviation: Int = 10 // Default: 10%
) {
    val logger = this.environment.log

    // Validate parameters
    if (password.isBlank()) {
        logger.error("Server Self-Update Misconfiguration: Password cannot be empty")
        return
    }

    if (serverJarPath.isBlank()) {
        logger.error("Server Self-Update Misconfiguration: Server JAR path cannot be empty")
        return
    }

    val serverJarFile = File(serverJarPath)
    val serverJarDir = serverJarFile.parentFile
    val serverJarName = serverJarFile.name
    val backupJarPath = File(serverJarDir, "backup_$serverJarName")

    // Validate that the server JAR exists
    if (!serverJarFile.exists() || !serverJarFile.isFile) {
        logger.error("Server Self-Update Misconfiguration: Server JAR file not found at $serverJarPath")
        return
    }

    // Check if restart script is provided
    val restartScriptFile = if (restartScriptPath != null) {
        if (restartScriptPath.isBlank()) {
            logger.error("Server Self-Update Misconfiguration: Restart script path cannot be empty if provided")
            return
        }

        val file = File(restartScriptPath)

        // Validate that the restart script exists and is executable
        if (!file.exists() || !file.isFile) {
            logger.error("Server Self-Update Misconfiguration: Restart script not found at $restartScriptPath")
            return
        }

        if (!file.canExecute()) {
            logger.error("Server Self-Update Misconfiguration: Restart script is not executable at $restartScriptPath")
            return
        }

        file
    } else {
        logger.info("Server Self-Update: No restart script provided. Server will not be automatically restarted after update.")
        null
    }

    // HTML template for the dark mode website
    val htmlTemplate = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Server Self-Update</title>
            <style>
                body {
                    font-family: Arial, sans-serif;
                    background-color: #121212;
                    color: #e0e0e0;
                    margin: 0;
                    padding: 20px;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    min-height: 100vh;
                }
                .container {
                    background-color: #1e1e1e;
                    border-radius: 8px;
                    padding: 30px;
                    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.3);
                    width: 100%;
                    max-width: 500px;
                }
                h1 {
                    color: #bb86fc;
                    margin-top: 0;
                    text-align: center;
                }
                form {
                    display: flex;
                    flex-direction: column;
                    gap: 20px;
                }
                label {
                    font-weight: bold;
                    margin-bottom: 5px;
                    display: block;
                }
                input[type="password"] {
                    padding: 10px;
                    border-radius: 4px;
                    border: 1px solid #333;
                    background-color: #2d2d2d;
                    color: #e0e0e0;
                    width: 100%;
                    box-sizing: border-box;
                }
                .file-input-container {
                    position: relative;
                }
                .file-input-label {
                    display: block;
                    padding: 10px;
                    background-color: #2d2d2d;
                    border: 1px solid #333;
                    border-radius: 4px;
                    text-align: center;
                    cursor: pointer;
                }
                input[type="file"] {
                    opacity: 0;
                    position: absolute;
                    top: 0;
                    left: 0;
                    width: 100%;
                    height: 100%;
                    cursor: pointer;
                }
                button {
                    padding: 12px;
                    background-color: #bb86fc;
                    color: #000;
                    border: none;
                    border-radius: 4px;
                    cursor: pointer;
                    font-weight: bold;
                    transition: background-color 0.3s;
                }
                button:hover {
                    background-color: #a370d8;
                }
                .message {
                    margin-top: 20px;
                    padding: 10px;
                    border-radius: 4px;
                    text-align: center;
                }
                .error {
                    background-color: rgba(255, 87, 87, 0.2);
                    color: #ff5757;
                }
                .success {
                    background-color: rgba(76, 175, 80, 0.2);
                    color: #4caf50;
                }
                .file-name {
                    margin-top: 5px;
                    font-size: 0.9em;
                    text-align: center;
                    word-break: break-all;
                }
            </style>
        </head>
        <body>
            <div class="container">
                <h1>Server Self-Update</h1>
                <form action="$updateEndpoint" method="post" enctype="multipart/form-data">
                    <div>
                        <label for="password">Password:</label>
                        <input type="password" id="password" name="$passwordParam" required>
                    </div>
                    <div>
                        <label for="jarFile">JAR File:</label>
                        <div class="file-input-container">
                            <label class="file-input-label" id="fileLabel">Select JAR file</label>
                            <input type="file" id="jarFile" name="jarFile" accept=".jar" required>
                        </div>
                        <div class="file-name" id="fileName"></div>
                    </div>
                    <button type="submit">Upload and Update</button>
                </form>
                <div id="message" class="message" style="display: none;"></div>
            </div>
            <script>
                document.getElementById('jarFile').addEventListener('change', function(e) {
                    const fileName = e.target.files[0] ? e.target.files[0].name : 'No file selected';
                    document.getElementById('fileName').textContent = fileName;
                    document.getElementById('fileLabel').textContent = 'Change file';
                });
            </script>
        </body>
        </html>
    """.trimIndent()

    // HTML template for success message
    fun getSuccessHtml(message: String) = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Server Self-Update</title>
            <style>
                body {
                    font-family: Arial, sans-serif;
                    background-color: #121212;
                    color: #e0e0e0;
                    margin: 0;
                    padding: 20px;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    min-height: 100vh;
                }
                .container {
                    background-color: #1e1e1e;
                    border-radius: 8px;
                    padding: 30px;
                    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.3);
                    width: 100%;
                    max-width: 500px;
                    text-align: center;
                }
                h1 {
                    color: #bb86fc;
                    margin-top: 0;
                }
                .success {
                    margin-top: 20px;
                    padding: 15px;
                    border-radius: 4px;
                    background-color: rgba(76, 175, 80, 0.2);
                    color: #4caf50;
                }
            </style>
        </head>
        <body>
            <div class="container">
                <h1>Server Self-Update</h1>
                <div class="success">$message</div>
            </div>
        </body>
        </html>
    """.trimIndent()

    // HTML template for error message
    fun getErrorHtml(message: String) = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Server Self-Update</title>
            <style>
                body {
                    font-family: Arial, sans-serif;
                    background-color: #121212;
                    color: #e0e0e0;
                    margin: 0;
                    padding: 20px;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    min-height: 100vh;
                }
                .container {
                    background-color: #1e1e1e;
                    border-radius: 8px;
                    padding: 30px;
                    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.3);
                    width: 100%;
                    max-width: 500px;
                    text-align: center;
                }
                h1 {
                    color: #bb86fc;
                    margin-top: 0;
                }
                .error {
                    margin-top: 20px;
                    padding: 15px;
                    border-radius: 4px;
                    background-color: rgba(255, 87, 87, 0.2);
                    color: #ff5757;
                }
                .back-button {
                    margin-top: 20px;
                    padding: 10px 15px;
                    background-color: #bb86fc;
                    color: #000;
                    border: none;
                    border-radius: 4px;
                    cursor: pointer;
                    font-weight: bold;
                    text-decoration: none;
                    display: inline-block;
                }
                .back-button:hover {
                    background-color: #a370d8;
                }
            </style>
        </head>
        <body>
            <div class="container">
                <h1>Server Self-Update</h1>
                <div class="error">$message</div>
                <a href="$updateEndpoint" class="back-button">Back to Upload</a>
            </div>
        </body>
        </html>
    """.trimIndent()

    // Configure the update endpoint
    routing {
        // GET handler to display the upload form
        get(updateEndpoint) {
            call.respondText(htmlTemplate, ContentType.Text.Html)
        }

        // POST handler to process the upload
        post(updateEndpoint) {
            var tempFile: File? = null
            var fileCount = 0

            try {
                // Process multipart data
                val multipart = call.receiveMultipart()
                var providedPassword: String? = null

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            if (part.name == passwordParam) {
                                providedPassword = part.value
                            }
                        }
                        is PartData.FileItem -> {
                            fileCount++

                            // Ensure only one file is being uploaded
                            if (fileCount > 1) {
                                logger.error("Server Self-Update: Multiple files received")
                                tempFile?.delete()
                                part.dispose()
                                call.respondText(
                                    getErrorHtml("Only one JAR file can be uploaded at a time"),
                                    ContentType.Text.Html
                                )
                                return@forEachPart
                            }

                            // Create a temporary file to store the uploaded JAR
                            tempFile = File.createTempFile("upload_", ".jar", serverJarDir)

                            // Save the uploaded file
                            part.streamProvider().use { input ->
                                tempFile!!.outputStream().use { output ->
                                    input.copyTo(output)
                                }
                            }

                            // Validate that the uploaded file is a JAR
                            if (!tempFile!!.isZipFile()) {
                                logger.error("Server Self-Update: Invalid file format - The uploaded file is not a valid JAR file")
                                tempFile.delete()
                                part.dispose()
                                call.respondText(
                                    getErrorHtml("The uploaded file is not a valid JAR file"),
                                    ContentType.Text.Html
                                )
                                return@forEachPart
                            }

                            // Validate file size
                            if (tempFile.length() > maxFileSize) {
                                logger.error("Server Self-Update: File too large - The uploaded file exceeds the maximum allowed size of ${maxFileSize / (1024 * 1024)} MB")
                                tempFile.delete()
                                part.dispose()
                                call.respondText(
                                    getErrorHtml("The uploaded file exceeds the maximum allowed size of ${maxFileSize / (1024 * 1024)} MB"),
                                    ContentType.Text.Html
                                )
                                return@forEachPart
                            }

                            // Validate file size deviation from current JAR
                            val currentFileSize = serverJarFile.length()
                            val uploadedFileSize = tempFile.length()
                            val deviation =
                                abs(uploadedFileSize - currentFileSize) * 100.0 / currentFileSize

                            if (deviation.toInt() > maxFileSizeDeviation) {
                                logger.error("Server Self-Update: File size deviation too large - The uploaded file size deviates by ${deviation.toInt()}% from the current file size, which exceeds the maximum allowed deviation of $maxFileSizeDeviation%")
                                tempFile.delete()
                                part.dispose()
                                call.respondText(
                                    getErrorHtml("The uploaded file size deviates by ${deviation.toInt()}% from the current file size, which exceeds the maximum allowed deviation of $maxFileSizeDeviation%"),
                                    ContentType.Text.Html
                                )
                                return@forEachPart
                            }
                        }
                        else -> {
                            // Ignore other part types
                        }
                    }
                    part.dispose()
                }

                // Validate password
                if (providedPassword != password) {
                    logger.warn("Server Self-Update: Unauthorized - Invalid password provided for update operation")
                    tempFile?.delete()
                    call.respondText(
                        getErrorHtml("Invalid password"),
                        ContentType.Text.Html
                    )
                    return@post
                }

                // Check if we received a file
                if (tempFile == null) {
                    logger.error("Server Self-Update: No file received")
                    call.respondText(
                        getErrorHtml("No file received. Please upload a single JAR file."),
                        ContentType.Text.Html
                    )
                    return@post
                }

                // Check if multiple files were attempted to be uploaded
                if (fileCount > 1) {
                    logger.error("Server Self-Update: Multiple files received")
                    call.respondText(
                        getErrorHtml("Only one JAR file can be uploaded at a time"),
                        ContentType.Text.Html
                    )
                    tempFile?.delete()
                    return@post
                }

                // Create a backup of the current JAR
                logger.info("Server Self-Update: Creating backup of current JAR at $backupJarPath")
                Files.copy(
                    serverJarFile.toPath(),
                    backupJarPath.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
                )

                // Replace the current JAR with the new one
                logger.info("Server Self-Update: Replacing current JAR with new version")
                Files.move(
                    tempFile.toPath(),
                    serverJarFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
                )

                // Respond with success before restarting (if applicable)
                val successMessage = if (restartScriptFile != null) {
                    "Update successful. Server is restarting..."
                } else {
                    "Update successful. Server will NOT be automatically restarted."
                }
                call.respondText(
                    getSuccessHtml(successMessage),
                    ContentType.Text.Html
                )

                // Execute the restart script in a separate thread after a short delay if provided
                if (restartScriptFile != null) {
                    thread {
                        try {
                            // Give time for the response to be sent
                            Thread.sleep(1000)

                            logger.info("Server Self-Update: Executing restart script at $restartScriptPath")
                            val process = ProcessBuilder(restartScriptPath!!).start()
                            val exitCode = process.waitFor()

                            if (exitCode != 0) {
                                logger.error("Server Self-Update: Restart script execution failed with exit code $exitCode")
                            }
                        } catch (e: Exception) {
                            logger.error("Server Self-Update: Error executing restart script: ${e.message}")
                        }
                    }
                } else {
                    logger.info("Server Self-Update: Update completed successfully, but server was not automatically restarted (no restart script provided)")
                }
            } catch (e: Exception) {
                logger.error("Server Self-Update: Error during update process: ${e.message}")
                call.respondText(
                    getErrorHtml("Error during update process: ${e.message}"),
                    ContentType.Text.Html
                )
                tempFile?.delete()
            }
        }
    }

    logger.info("Server Self-Update: Endpoint configured at $updateEndpoint")
}
