package kotnexlib.storage

import kotnexlib.ExperimentalKotNexLibAPI
import kotnexlib.ResultOf
import kotnexlib.toDate
import java.io.File

/**
 * Represents a file or directory located on a remote system, typically accessed via SSH.
 *
 * @property absolutePath The absolute path to the remote file or directory.
 * @property isDirectory Indicates whether this instance represents a directory. If false, it represents a file.
 * @property size The size of the file or directory, in bytes.
 * @property lastModified The timestamp of the last modification in milliseconds since the epoch. May be null if not available.
 */
data class RemoteFile(
    val absolutePath: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Long? = null,
) {
    /**
     * Deletes the remote file or folder represented by this instance.
     *
     * If this instance represents a directory, all its contents will be removed recursively.
     * Otherwise, the file at the specified path will be deleted.
     *
     * Delegates to [SshStorage.deleteFolder] for directories and [SshStorage.deleteFile] for files.
     *
     * @return A [kotnexlib.ResultOfEmpty] indicating the success or failure of the operation.
     */
    fun delete() = if (isDirectory) SshStorage.deleteFolder(absolutePath) else SshStorage.deleteFile(absolutePath)

    /**
     * Downloads this remote file or directory to a specified local destination.
     *
     * If this instance represents a directory, the folder and its contents will be downloaded.
     * If this instance represents a file, only the file will be downloaded.
     *
     * The download operation delegates to [SshStorage.downloadFolder] for directories and
     * [SshStorage.downloadFile] for individual files.
     *
     * @param localDestination The local file or directory where the remote file/directory will be downloaded.
     */
    fun download(localDestination: File) =
        if (isDirectory) SshStorage.downloadFolder(absolutePath, localDestination) else SshStorage.downloadFile(
            absolutePath,
            localDestination
        )

    /**
     * Downloads an encrypted file from the remote storage location, decrypts its content,
     * and optionally saves it to a local destination.
     *
     * If the provided `localDestination` parameter is not null, the decrypted content is written
     * to the specified file. If the instance represents a directory, this operation is not supported.
     *
     * @param password The password used for decrypting the file content.
     * @param localDestination An optional local file to save the decrypted content. If null, the content
     *                         is returned as a byte array without being saved.
     * @return A [ResultOf] containing the decrypted byte array on success or an error message with additional
     *         information on failure.
     * @throws IllegalArgumentException if the instance represents a directory, as directory encryption/decryption
     *         is not supported.
     */
    @ExperimentalKotNexLibAPI
    fun downloadAndDecrypt(password: String, localDestination: File? = null): ResultOf<ByteArray> {
        if (isDirectory) throw IllegalArgumentException("Directory encryption/decryption not supported yet.")
        return SshStorage.Encryption.download(absolutePath, password).also {
            if (localDestination != null) it.getOrNull()?.let { bytes -> localDestination.writeBytes(bytes) }
        }
    }

    val name get() = absolutePath.substringAfterLast("/")
    val lastModifiedDate = lastModified?.toDate()
    val isFile get() = !isDirectory
}