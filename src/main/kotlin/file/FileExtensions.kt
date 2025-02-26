package file

import kotnexlib.ResultOf2
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
        if (isDirectory) true
        else if (createIfNotExist) mkdirs()
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
        if (isFile) true
        else if (createIfNotExist) createNewFile()
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
    parentFolderToStoreZip: File = File(parentFile.absolutePath),
    zipName: String = "$nameWithoutExtension.zip"
): ZipResult {
    val zipFile = File(parentFolderToStoreZip, zipName)
    return ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zipOut ->
        val files = if (isDirectory) listFiles() else arrayOf(this)

        if (files == null) return ZipResult.Failure("listFiles is null.")
        else if (files.isEmpty()) return ZipResult.FolderIsEmpty

        try {
            zipFilesRecursive(this, zipOut)
        } catch (e: Exception) {
            return ZipResult.Failure("Error while zipping. -> Exception: ${e.stackTraceToString()}")
        }
        ZipResult.Success(zipFile)
    }
}

/**
 * ToDo: Es wird dadurch keine gültige Zip erstellt. Es scheint der Header oder so zu fehlen.
 *  Muss noch geprüft werden, warum das passiert.
 */
private fun File.zipFilesToByteArray(): ResultOf2<ByteArray, String> {
    val byteArrayOutputStream = ByteArrayOutputStream()
    return ZipOutputStream(BufferedOutputStream(byteArrayOutputStream)).use { zipOut ->
        val files = if (isDirectory) listFiles() else arrayOf(this)

        if (files == null) return ResultOf2.Failure("listFiles is null.")
        else if (files.isEmpty()) return ResultOf2.Failure("Folder is empty! No files to Zip.")

        try {
            zipFilesRecursive(this, zipOut)
        } catch (e: Exception) {
            return ResultOf2.Failure("Error while zipping. -> Exception: ${e.stackTraceToString()}")
        }

        ResultOf2.Success(byteArrayOutputStream.toByteArray())
    }
}

private fun zipFilesRecursive(file: File, zipOut: ZipOutputStream, basePath: String = "") {
    val files = if (file.isDirectory) file.listFiles() else arrayOf(file)

    files.forEach { file ->
        val relativePath = basePath + file.name
        if (file.isDirectory) {
            zipFilesRecursive(file, zipOut, "$relativePath${File.separator}")
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
 * If the `deleteAfterUnzip` flag is set to true, the original zip file will be deleted after the operation.
 * The `ignoreZipFileCheck` parameter allows skipping validation of whether the file is a ZIP file.
 *
 * @param destinationFolder The folder where the contents of the zip file will be extracted. Defaults to a folder
 *                          in the same directory as the zip file, with the same name as the zip file (without the extension).
 * @param deleteAfterUnzip  Indicates whether the zip file should be deleted after extraction. Defaults to false.
 * @param ignoreZipFileCheck If true, skips checking if the file is a valid ZIP file. Use with caution. Defaults to false.
 * @return [UnzipResult] indicating the success or failure of the operation.
 */
fun File.unzipFile(
    destinationFolder: File = File(parentFile, nameWithoutExtension),
    deleteAfterUnzip: Boolean = false,
    ignoreZipFileCheck: Boolean = false
): UnzipResult {
    if (isFile.not()) return UnzipResult.Failure("The provided file is not a valid file.")
    if (ignoreZipFileCheck.not() && isZipFile().not()) return UnzipResult.NotAZipFile

    if (destinationFolder.exists()) destinationFolder.deleteRecursively()
    if (!destinationFolder.existsDir(createIfNotExist = true)) {
        return UnzipResult.Failure("Failed to create destination directory: ${destinationFolder.absolutePath}")
    }

    return try {
        BufferedInputStream(FileInputStream(this)).use { fileInputStream ->
            ZipInputStream(fileInputStream).use { zipIn ->
                var entry: ZipEntry? = zipIn.nextEntry

                while (entry != null) {
                    val file = File(destinationFolder, entry.name)

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
        if (bytesRead == 4) (header[0] == 0x50.toByte() && header[1] == 0x4B.toByte() && header[2] == 0x03.toByte() && header[3] == 0x04.toByte())
        else false
    }
} catch (ignore: Exception) {
    false
}



