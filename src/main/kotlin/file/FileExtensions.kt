package file

import java.io.File

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