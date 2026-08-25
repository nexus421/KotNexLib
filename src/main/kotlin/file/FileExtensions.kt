package file

import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Checks if this file is a directory and exists.
 * If [createIfNotExist] is true than it will create the directory.
 *
 * @return true if this file exists and is a directory. false if this file does not exist or is not a directory and/or creation was not wished or failed.
 */
fun File.existsDir(createIfNotExist: Boolean = true): Boolean {
    return if (exists()) {
        isDirectory || if (createIfNotExist) mkdirs()
        else false
    } else if (createIfNotExist) mkdirs()
    else false
}

/**
 * Checks if this file is a file and exists.
 * If [createIfNotExist] is true than it will create the file.
 *
 * @return true if this file exists and is a file. false if this file does not exist or is not a file and/or creation was not wished or failed.
 */
fun File.existsFile(createIfNotExist: Boolean = true): Boolean {
    return if (exists()) {
        isFile || if (createIfNotExist) createNewFile()
        else false
    } else if (createIfNotExist) createNewFile()
    else false
}

/**
 * This will zip all files/folders inside this folder or this single File.
 *
 * @param parentFolderToStoreZip destination where the zipfile will be stored. Defaults to the parent of this File.
 * @param zipName name of the zip file. Defaults to the original folder name of this with ".zip" extension.
 *
 * @return the zip file oder an failure with the error message.
 */
fun File.zipFiles(
    parentFolderToStoreZip: File = parentFile ?: File(absoluteFile.parent ?: "."),
    zipName: String = "$nameWithoutExtension.zip"
): ZipResult {
    val files = if (isDirectory) listFiles() else arrayOf(this)

    if (files == null) return ZipResult.Failure("listFiles is null.")
    else if (files.isEmpty()) return ZipResult.FolderIsEmpty

    val zipFile = File(parentFolderToStoreZip, zipName)
    return try {
        ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zipOut ->
            zipFilesRecursive(this, zipOut)
        }
        ZipResult.Success(zipFile)
    } catch (e: Exception) {
        zipFile.delete()
        ZipResult.Failure("Error while zipping. -> Exception: ${e.stackTraceToString()}")
    }
}

private fun zipFilesRecursive(file: File, zipOut: ZipOutputStream, basePath: String = "") {
    val files = if (file.isDirectory) file.listFiles() else arrayOf(file)

    files.forEach { file ->
        val relativePath = basePath + file.name
        if (file.isDirectory) {
            zipFilesRecursive(file, zipOut, "$relativePath/")
        } else {
            BufferedInputStream(FileInputStream(file)).use {
                val entry = ZipEntry(relativePath)
                zipOut.putNextEntry(entry)
                it.copyTo(zipOut)
                zipOut.closeEntry()
            }
        }
    }
}

/**
 * Unzips the file represented by this `File` instance into the specified destination folder.
 *
 * @param destinationFolder The folder where the contents of the zip file will be extracted.
 *                          If it already exists, its contents will be deleted recursively before extraction.
 * @param ignoreZipFileCheck If true, skips the validation check to verify if the file is a valid zip file. Default is false.
 * @param deleteAfterUnzip If true, deletes the original zip file after the extraction is completed. Default is false.
 * @return An [UnzipResult] indicating the outcome of the operation:
 *         - [UnzipResult.Success]: If the extraction was successful, containing the destination folder.
 *         - [UnzipResult.NotAZipFile]: If the file is not a valid zip file and [ignoreZipFileCheck] is false.
 *         - [UnzipResult.Failure]: If an error occurs during the operation, with an error message describing the failure.
 */
fun File.unzipFile(
    destinationFolder: File = File(parentFile ?: File(absoluteFile.parent ?: "."), nameWithoutExtension),
    deleteAfterUnzip: Boolean = false,
    ignoreZipFileCheck: Boolean = false
): UnzipResult {
    if (exists().not()) return UnzipResult.Failure("File does not exist: $absolutePath")
    if (isFile.not()) return UnzipResult.Failure("The provided file is not a valid file.")
    if (ignoreZipFileCheck.not() && isZipFile().not()) return UnzipResult.NotAZipFile

    if (destinationFolder.exists()) destinationFolder.deleteRecursively()
    if (!destinationFolder.existsDir(createIfNotExist = true)) {
        return UnzipResult.Failure("Failed to create destination directory: ${destinationFolder.absolutePath}")
    }

    val canonicalDestPath = destinationFolder.canonicalFile.toPath()

    return try {
        BufferedInputStream(FileInputStream(this)).use { fileInputStream ->
            ZipInputStream(fileInputStream).use { zipIn ->
                var entry: ZipEntry? = zipIn.nextEntry

                while (entry != null) {
                    val file = File(destinationFolder, entry.name)
                    val canonicalFilePath = file.canonicalFile.toPath()
                    if (!canonicalFilePath.startsWith(canonicalDestPath)) {
                        return UnzipResult.Failure("Zip slip security violation: entry '${entry.name}' targets outside destination folder.")
                    }

                    if (entry.isDirectory) {
                        if (!file.existsDir(createIfNotExist = true)) {
                            return UnzipResult.Failure("Failed to create directory: ${file.absolutePath}")
                        }
                    } else {
                        if (!file.parentFile.existsDir(createIfNotExist = true)) {
                            return UnzipResult.Failure("Failed to create parent directory for file: ${file.absolutePath}")
                        }
                        BufferedOutputStream(FileOutputStream(file)).use { output ->
                            zipIn.copyTo(output)
                        }
                    }
                    zipIn.closeEntry()
                    entry = zipIn.nextEntry
                }
            }
        }
        if (deleteAfterUnzip) delete()
        UnzipResult.Success(destinationFolder)
    } catch (e: Exception) {
        UnzipResult.Failure("An error occurred during unzip operation: ${e.message}")
    }
}

sealed interface UnzipResult {
    data class Success(val destinationFolder: File) : UnzipResult
    data class Failure(val errorMessage: String) : UnzipResult
    data object NotAZipFile : UnzipResult
}

/**
 * Use this file within [file]. Afterward the file will be deleted.
 */
fun <T> File.useAndDelete(file: File.() -> T): T {
    val result = file()
    delete()
    return result
}

sealed interface ZipResult {
    data class Success(val result: File) : ZipResult
    data class Failure(val error: String) : ZipResult
    data object FolderIsEmpty : ZipResult
}

/**
 * Determines if the current file is a valid ZIP file by checking its first four bytes.
 *
 * @return true if the file has a ZIP file signature, otherwise false
 */
fun File.isZipFile(): Boolean = try {
    FileInputStream(this).use { fis ->
        val header = ByteArray(4)
        val bytesRead = fis.read(header)
        bytesRead == 4 && (header[0] == 0x50.toByte() && header[1] == 0x4B.toByte() && header[2] == 0x03.toByte() && header[3] == 0x04.toByte())
    }
} catch (ignore: Exception) {
    false
}



