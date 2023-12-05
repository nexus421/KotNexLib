package file

import ResultOf2
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
 * This will zip all files inside this folder. This may not check for sub folders!
 *
 * @param folderToStoreZip destination where the zipped file will be stored
 * @param zipName name of the zip file. Defaults to the original folder name of this with ".zip" extension.
 *
 * @return the zip file oder an failure with the error message.
 */
fun File.zipFiles(folderToStoreZip: File, zipName: String = "$name.zip"): ResultOf2<File, String> {
    val zipFile = File(folderToStoreZip, zipName)
    return ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zipOut ->
        val files = listFiles()

        if (files == null) return ResultOf2.Failure("listFiles is null.")
        else if (files.isEmpty()) return ResultOf2.Failure("Folder is empty! No files to Zip.")

        try {
            files.forEach { file ->
                BufferedInputStream(FileInputStream(file)).use {
                    val entry = ZipEntry(file.name)
                    zipOut.putNextEntry(entry)
                    it.copyTo(zipOut)
                    zipOut.closeEntry()
                }
            }
        } catch (e: Exception) {
            return ResultOf2.Failure("Error while zipping. -> Exception: ${e.stackTraceToString()}")
        }
        ResultOf2.Success(zipFile)
    }
}
