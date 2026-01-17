package kotnexlib.storage

import kotnexlib.ResultOf
import kotnexlib.ResultOfEmpty
import kotnexlib.tryOrNull
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

data class RemoteFile(
    val absolutePath: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Long? = null,
//    val creationDate: Long? = null
)

object SshStorage {

    private lateinit var sshKey: File
    private lateinit var user: String
    private lateinit var host: String
    private var port: Int = 22

    fun init(sshKey: File, user: String, host: String, port: Int = 22) {
        this.sshKey = sshKey
        this.user = user
        this.host = host
        this.port = port
    }

    private fun checkInit() {
        require(::sshKey.isInitialized) { "SshStorage not initialized. Call init() first." }
        require(::user.isInitialized) { "SshStorage not initialized. Call init() first." }
        require(::host.isInitialized) { "SshStorage not initialized. Call init() first." }
    }

    fun uploadFile(
        fileToUpload: File,
        destinationPath: String
    ): ResultOfEmpty<String> {
        checkInit()

        val remoteTarget = if (destinationPath.endsWith("/") || destinationPath.isEmpty()) {
            destinationPath + fileToUpload.name
        } else destinationPath


        val command = listOf(
            "scp",
            "-P", port.toString(),
            "-i", sshKey.absolutePath,
            "-o", "StrictHostKeyChecking=accept-new",
            fileToUpload.absolutePath,
            "$user@$host:${remoteTarget.removeStartingSlash()}"
        )

        return executeCommand(command)
    }

    fun downloadFile(
        remotePath: String,
        localDestination: File
    ): ResultOfEmpty<String> {
        checkInit()

        val finalLocalDestination = if (localDestination.isDirectory) {
            File(localDestination, remotePath.substringAfterLast('/'))
        } else localDestination

        val command = listOf(
            "scp",
            "-P", port.toString(),
            "-i", sshKey.absolutePath,
            "-o", "StrictHostKeyChecking=accept-new",
            "$user@$host:${remotePath.removeStartingSlash()}",
            finalLocalDestination.absolutePath
        )

        return executeCommand(command)
    }

    fun listFiles(directoryPath: String): ResultOf<List<RemoteFile>> {
        checkInit()

        // ls -la --time-style=long-iso: liefert Details inklusive Datum in festem Format
        val command = listOf(
            "ssh",
            "-p", port.toString(),
            "-i", sshKey.absolutePath,
            "-o", "StrictHostKeyChecking=accept-new",
            "$user@$host",
            "ls -la --time-style=long-iso ${directoryPath.removeStartingSlash()}"
        )

        return try {
            val process = ProcessBuilder(command).start()
            val output = process.inputStream.bufferedReader().use { it.readText() }
            val error = process.errorStream.bufferedReader().use { it.readText() }
            val finished = process.waitFor(1, TimeUnit.MINUTES)

            if (finished && process.exitValue() == 0) {
                val lines = output.lines().filter { it.isNotBlank() }
                val files = mutableListOf<RemoteFile>()

                // Die erste Zeile bei ls -la ist "total X", falls es ein Verzeichnis ist
                val startIndex = if (lines.firstOrNull()?.startsWith("total") == true) 1 else 0

                for (i in startIndex until lines.size) {
                    val line = lines[i]
                    val parts = line.split(Regex("\\s+"))
                    if (parts.size < 8) continue

                    // drwxr-xr-x 2 user group 4096 2023-10-27 10:30 .
                    val permissions = parts[0]
                    val size = parts[4].toLongOrNull() ?: 0L
                    val dateStr = parts[5] // 2023-10-27
                    val timeStr = parts[6] // 10:30
                    val name = parts.subList(7, parts.size).joinToString(" ")

                    if (name == "." || name == "..") continue

                    val isDir = permissions.startsWith("d")

                    val lastModified = tryOrNull {
                        LocalDateTime.parse("$dateStr $timeStr", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                            .atZone(ZoneOffset.UTC)
                            .toInstant()
                            .toEpochMilli()
                    }

                    val cleanDirPath = directoryPath.removeTrailingSlash()
                    val fullPath = if (isDir) {
                        "$cleanDirPath/$name/"
                    } else {
                        // Bugfix: Wenn directoryPath eine Datei ist, wird ls oft nur den Namen der Datei listen
                        if (cleanDirPath.endsWith(name)) cleanDirPath else "$cleanDirPath/$name"
                    }

                    files.add(
                        RemoteFile(
                            absolutePath = fullPath,
                            isDirectory = isDir,
                            size = size,
                            lastModified = lastModified
                        )
                    )
                }
                ResultOf.Success(files)
            } else {
                ResultOf.Failure("Exit code: ${process.exitValue()}\nError: $error")
            }
        } catch (e: Exception) {
            ResultOf.Failure(e.message ?: "Unknown Error", e)
        }
    }

    private fun executeCommand(command: List<String>): ResultOfEmpty<String> {

        return try {
            val process = ProcessBuilder(command).start()
            val error = process.errorStream.bufferedReader().use { it.readText() }
            val finished = process.waitFor(5, TimeUnit.MINUTES)

            if (finished && process.exitValue() == 0) {
                ResultOfEmpty.Success
            } else {
                ResultOfEmpty.Failure("Exit code: ${process.exitValue()}\nError: $error")
            }
        } catch (e: Exception) {
            ResultOfEmpty.Failure(e.message ?: "Unknown Error")
        }
    }

    private fun String.removeStartingSlash() = if (startsWith("/")) drop(1) else this
    private fun String.removeTrailingSlash() = if (endsWith("/")) dropLast(1) else this
}