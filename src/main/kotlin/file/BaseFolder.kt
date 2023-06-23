package bayern.kickner.kotlin_extensions_android.file

import bayern.kickner.kotlin_extensions_android.ifFalse
import java.io.File

/**
 * Checks/Creates a folder at the given location. This can be used as your working directory.
 *
 * @param path to your root working directory. Do not use a separator at the and! Defaults to user.home
 * @param name of the workingDirFolder
 */
open class BaseFolder(path: String = System.getProperty("user.home"), name: String) {

    val baseFolder = File(path + File.separator + name)

    init {
        if (baseFolder.exists()) {
            if (baseFolder.isDirectory.not()) {
                if (baseFolder.mkdirs()) println("Created base folder at ${baseFolder.absolutePath}")
                else println("Could not create base folder at ${baseFolder.absolutePath}")
            } else println("Base folder exists at ${baseFolder.absolutePath}")
        } else {
            if (baseFolder.mkdirs()) println("Created base folder at ${baseFolder.absolutePath}")
            else println("Could not create base folder at ${baseFolder.absolutePath}")
        }
    }


}