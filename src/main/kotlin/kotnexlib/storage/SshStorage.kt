package kotnexlib.storage

import kotnexlib.ExperimentalKotNexLibAPI
import kotnexlib.ResultOf
import kotnexlib.ResultOfEmpty
import kotnexlib.crypto.AES
import kotnexlib.storage.SshStorage.upload
import kotnexlib.tryOrNull
import java.io.File
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

/**
 * Provides an object for interacting with remote systems using SSH for tasks such as file upload, download, and directory listing.
 * This class utilizes SCP (Secure Copy Protocol) for file transfers and SSH commands for directory operations.
 *
 * Requires SCP and SSH being installed and available in the PATH.
 * Requires a valid SSH key to be present in the specified location. SSH password is not supported.
 *
 * Supports Hetzner Storage Box.
 */
object SshStorage {

    private lateinit var sshKey: File
    private lateinit var user: String
    private lateinit var host: String
    private var port: Int = 22

    /**
     * Data used for SSH/SCP connection.
     *
     * @param sshKey The file containing the private SSH key used for authentication.
     * @param user The username for the SSH login.
     * @param host The hostname or IP address of the remote server.
     * @param port The port number for the SSH connection. Defaults to 22 if not specified.
     */
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

    /**
     * Provides methods for encrypted file operations on the remote storage.
     * Uses AES GCM encryption with password-based key derivation.
     */
    @ExperimentalKotNexLibAPI
    object Encryption {

        /**
         * Encrypts and uploads string data to a remote destination.
         *
         * @param data The string content to be encrypted and uploaded.
         * @param filename The name of the file to be created on the remote host.
         * @param password The password used for encryption.
         * @param destinationPath The remote directory path.
         */
        fun upload(data: String, filename: String, password: String, destinationPath: String = "") =
            upload(data.toByteArray(), filename, password, destinationPath)

        /**
         * Encrypts and uploads a local file to a remote destination.
         *
         * @param file The local file to be encrypted and uploaded.
         * @param password The password used for encryption.
         * @param destinationPath The remote directory path.
         */
        fun upload(file: File, password: String, destinationPath: String = "") =
            upload(file.readBytes(), file.name, password, destinationPath)

        /**
         * Encrypts and uploads a byte array to a remote destination.
         *
         * @param data The byte array to be encrypted and uploaded.
         * @param filename The name of the file to be created on the remote host.
         * @param password The password used for encryption.
         * @param destinationPath The remote directory path.
         * @return A [ResultOfEmpty] indicating success or failure.
         */
        fun upload(
            data: ByteArray,
            filename: String,
            password: String,
            destinationPath: String
        ): ResultOfEmpty<String> {
            val encrypted = AES.GCM.encryptWithPassword(data, password)
            val tmpFile = File.createTempFile("kotnexlib_encrypted_temp", ".tmp")
            tmpFile.writeText(encrypted.toString())
            return SshStorage.upload(tmpFile, destinationPath + File.separator + filename)
        }

        /**
         * Downloads an encrypted file from a remote path, decrypts its content, and writes the contents to a local file.
         *
         * @param destination The local file where the decrypted data will be saved.
         * @param remotePath The path of the encrypted file on the remote host.
         * @param password The password required for decryption of the downloaded file.
         * @return A [ResultOfEmpty] indicating success or failure of the download operation.
         */
        fun downloadToFile(destination: File, remotePath: String, password: String) =
            when (val result = download(remotePath, password)) {
                is ResultOf.Failure -> ResultOfEmpty.Failure(result.message)
                is ResultOf.Success -> {
                    destination.writeBytes(result.value)
                    ResultOfEmpty.Success
                }
            }

        /**
         * Downloads an encrypted file from the remote host and decrypts it.
         *
         * @param remotePath The path to the encrypted file on the remote host.
         * @param password The password used for decryption.
         * @param destinationFile The local file where the decrypted content will be saved, if not null.
         * @return A [ResultOf] containing the decrypted byte array on success.
         */
        fun download(remotePath: String, password: String, destinationFile: File? = null): ResultOf<ByteArray> {
            val tmpFile = File.createTempFile("kotnexlib_download_temp", ".tmp")
            try {
                val downloadRes = downloadFile(remotePath, tmpFile)
                if (downloadRes is ResultOfEmpty.Failure) return ResultOf.Failure(downloadRes.value)

                val encryptedText = tmpFile.readText()
                val aesData = AES.AESData.restore(encryptedText).getOrNull()
                    ?: return ResultOf.Failure("Could not restore AESData")
                val decrypted =
                    aesData.decryptAsByteArray(password).getOrNull() ?: return ResultOf.Failure("Decryption failed")

                destinationFile?.writeBytes(decrypted)

                return ResultOf.Success(decrypted)
            } catch (e: Exception) {
                return ResultOf.Failure(e.message ?: "Unknown error", e)
            } finally {
                tmpFile.delete()
            }
        }

        /**
         * Downloads an encrypted file from the remote host, decrypts it, and saves it to a local file.
         *
         * @param remotePath The path to the encrypted file on the remote host.
         * @param password The password used for decryption.
         * @param localFile The local file where the decrypted content will be saved.
         * @return A [ResultOfEmpty] indicating success or failure.
         */
        fun downloadToFile(remotePath: String, password: String, localFile: File): ResultOfEmpty<String> {
            return when (val res = download(remotePath, password)) {
                is ResultOf.Success -> {
                    try {
                        localFile.writeBytes(res.value)
                        ResultOfEmpty.Success
                    } catch (e: Exception) {
                        ResultOfEmpty.Failure(e.message ?: "Could not write to local file")
                    }
                }

                is ResultOf.Failure -> ResultOfEmpty.Failure(res.message ?: "Download failed")
            }
        }
    }

    /**
     * Uploads a byte array as a file to a remote destination. The data is written to a temporary file for the upload.
     * The temporary file will be deleted after the upload is complete.
     *
     * @param byteArray The content to be uploaded as a byte array.
     * @param filename The name of the file to be created or replaced on the remote destination.
     * @param destinationPath The remote directory path where the file will be uploaded. Defaults to an empty string,
     *                        which means the file will be uploaded to the user's home directory on the remote host.
     * @return A [ResultOfEmpty] indicating the success status of the upload operation.
     */
    fun upload(byteArray: ByteArray, filename: String, destinationPath: String = ""): ResultOfEmpty<String> {
        val tempFile = File.createTempFile("kotnexlib_temp", ".tmp")
        try {
            tempFile.writeBytes(byteArray)
            return upload(tempFile, destinationPath + File.separator + filename)
        } finally {
            tempFile.delete()
        }
    }

    /**
     * Uploads a text data to a remote destination. Uses [upload] with ByteArray internally.
     *
     * @param text The string content to be uploaded.
     * @param filename The name of the file to be created or replaced on the remote destination.
     * @param destinationPath The remote directory path where the file will be uploaded. Defaults to an empty string,
     *                        which means the file will be uploaded to the user's home directory on the remote host.
     */
    fun upload(text: String, filename: String, destinationPath: String = "") =
        upload(text.toByteArray(), filename, destinationPath)

    /**
     * Uploads a specified file to a remote destination using the SCP (Secure Copy Protocol).
     *
     * @param fileToUpload The file to be uploaded. It can be a file or a directory.
     * @param destinationPath The remote path where the file will be uploaded. Defaults to an empty string,
     *                        which means the file will be uploaded to the user's home directory on the remote host.
     *                        If the path ends with a slash, the file will be placed inside that directory.
     *                        If the path is a specific file, it will replace/create that file on the destination.
     * @return A [ResultOfEmpty] indicating the success status of the upload operation.
     *         Returns [ResultOfEmpty.Success] if the file is uploaded successfully,
     *         or [ResultOfEmpty.Failure] with an error message if the operation fails.
     */
    fun upload(fileToUpload: File, destinationPath: String = ""): ResultOfEmpty<String> {
        checkInit()

        val remoteTarget = if (destinationPath.endsWith("/") || destinationPath.isEmpty()) {
            destinationPath + fileToUpload.name
        } else destinationPath

        val command = buildList {
            add("scp")
            add("-P")
            add(port.toString())
            if (fileToUpload.isDirectory) add("-r")
            add("-i")
            add(sshKey.absolutePath)
            add("-o")
            add("StrictHostKeyChecking=accept-new")
            add(fileToUpload.absolutePath)
            add("$user@$host:${remoteTarget.removeStartingSlash()}")
        }

        return executeCommand(command)
    }

    /**
     * Downloads a file from a remote location to a local destination using SCP.
     *
     * @param remotePath The absolute or relative path of the remote file to be downloaded.
     *                   If relative, it is relative to the SSH user's home directory.
     * @param localDestination The local destination file where the downloaded content will be stored.
     */
    fun downloadFile(remotePath: String, localDestination: File) = download(remotePath, localDestination, isFile = true)

    /**
     * Downloads a remote folder to a specified local destination.
     *
     * @param remotePath The path to the remote folder that should be downloaded.
     * @param localDestination The local directory where the remote folder will be downloaded.
     */
    fun downloadFolder(remotePath: String, localDestination: File) =
        download(remotePath, localDestination, isFile = false)

    /**
     * Deletes a file on the remote host.
     *
     * @param remotePath The path to the file on the remote host.
     * @return A [ResultOfEmpty] denoting the success or failure of the operation.
     */
    fun deleteFile(remotePath: String) = delete(remotePath, isFolder = false)

    /**
     * Deletes a folder and all its contents on the remote host.
     *
     * @param remotePath The path to the folder on the remote host.
     * @return A [ResultOfEmpty] denoting the success or failure of the operation.
     */
    fun deleteFolder(remotePath: String) = delete(remotePath, isFolder = true)

    /**
     * Deletes a file or folder on the remote host using SSH.
     *
     * @param remotePath The path to the resource on the remote host.
     * @param isFolder A flag indicating whether the resource is a folder (`true`) or a file (`false`).
     * @return A [ResultOfEmpty] denoting the success or failure of the operation.
     */
    private fun delete(remotePath: String, isFolder: Boolean): ResultOfEmpty<String> {
        checkInit()

        val rmCommand = if (isFolder) "rm -rf" else "rm"
        val command = listOf(
            "ssh",
            "-p", port.toString(),
            "-i", sshKey.absolutePath,
            "-o", "StrictHostKeyChecking=accept-new",
            "$user@$host",
            "$rmCommand ${remotePath.removeStartingSlash()}"
        )

        return executeCommand(command)
    }

    /**
     * Downloads a file or folder from a remote path to a local destination using SCP.
     *
     * @param remotePath The absolute or relative path of the remote resource to be downloaded.
     *                   If the path is relative, it is resolved relative to the SSH user's home directory.
     * @param localDestination The local destination where the downloaded file or folder will be stored.
     * @param isFile A flag indicating whether the remote resource is a file (`true`) or a folder (`false`).
     *               If `false`, a recursive SCP download will be performed.
     * @return A [ResultOfEmpty] denoting the success or failure of the operation.
     *         On success, [ResultOfEmpty.Success] is returned. On failure, a [ResultOfEmpty.Failure]
     *         containing the error message is returned.
     */
    private fun download(remotePath: String, localDestination: File, isFile: Boolean): ResultOfEmpty<String> {
        checkInit()

        val command = buildList {
            add("scp")
            add("-P")
            add(port.toString())
            if (isFile.not()) add("-r") //important, if remote destination is a folder
            add("-i")
            add(sshKey.absolutePath)
            add("-o")
            add("StrictHostKeyChecking=accept-new")
            add("$user@$host:${remotePath.removeStartingSlash()}")
            add(localDestination.absolutePath)
        }

        return executeCommand(command)
    }

    private val splitPattern = "\\s+".toRegex()
    private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm").apply {
        timeZone = java.util.TimeZone.getTimeZone("UTC")
    }

    /**
     * Lists files and directories within a specified remote directory using SSH.
     *
     * @param directoryPath The absolute path to the remote directory whose contents are to be listed.
     * @return A [ResultOf] containing a list of [RemoteFile] objects on success, or a failure message and throwable on error.
     */
    fun listFiles(directoryPath: String): ResultOf<List<RemoteFile>> {
        checkInit()

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

                // first line for ls -la is "total X", if it's a folder
                val dropFirstLines = if (lines.firstOrNull()?.startsWith("total") == true) 1 else 0

                val files = lines
                    .drop(dropFirstLines)
                    .map { it.split(splitPattern) }
                    .filter { it.size >= 8 }
                    .mapNotNull { parts ->
                        // drwxr-xr-x 2 user group 4096 2023-10-27 10:30 .
                        val permissions = parts[0]
                        val size = parts[4].toLongOrNull() ?: 0L
                        val dateStr = parts[5] // 2023-10-27
                        val timeStr = parts[6] // 10:30
                        val name = parts.subList(7, parts.size).joinToString(" ")

                        if (name == "." || name == "..") return@mapNotNull null

                        val isDir = permissions.startsWith("d")

                        val lastModified = tryOrNull { sdf.parse("$dateStr $timeStr").time }

                        val cleanDirPath = directoryPath.removeTrailingSlash()
                        val fullPath = if (isDir) "$cleanDirPath/$name/"
                        else if (cleanDirPath.endsWith(name)) cleanDirPath else "$cleanDirPath/$name"

                        RemoteFile(
                            absolutePath = fullPath,
                            isDirectory = isDir,
                            size = size,
                            lastModified = lastModified
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

    /**
     * Executes a shell command with a specified timeout duration and returns the result.
     *
     * @param command The command to execute as a list of strings, where the first element
     *                is the executable and the subsequent elements are its arguments.
     * @param timeoutMinutes The maximum time in minutes to wait for the process to complete.
     *                       Defaults to 5 minutes.
     * @return A [ResultOfEmpty] object representing the outcome of the command execution.
     *         - If the process finishes successfully with an exit code of `0`, returns [ResultOfEmpty.Success].
     *         - Otherwise, returns [ResultOfEmpty.Failure] containing the exit code and error message, 
     *           or an exception message in case of a failure to start or execute the process.
     */
    private fun executeCommand(command: List<String>, timeoutMinutes: Long = 5): ResultOfEmpty<String> {

        return try {
            val process = ProcessBuilder(command).start()
            val error = process.errorStream.bufferedReader().use { it.readText() }
            val finished = process.waitFor(timeoutMinutes, TimeUnit.MINUTES)

            if (finished && process.exitValue() == 0) ResultOfEmpty.Success
            else ResultOfEmpty.Failure("Exit code: ${process.exitValue()}\nError: $error")

        } catch (e: Exception) {
            ResultOfEmpty.Failure(e.message ?: "Unknown Error")
        }
    }

    private fun String.removeStartingSlash() = if (startsWith("/")) drop(1) else this
    private fun String.removeTrailingSlash() = if (endsWith("/")) dropLast(1) else this
}