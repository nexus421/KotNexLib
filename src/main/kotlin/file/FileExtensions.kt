package file

import kotnexlib.ResultOf2
import java.io.*
import java.util.zip.ZipEntry
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

