package file

import ifTrue
import java.io.File

/**
 * Checks/Creates a folder at the given location. This can be used as your working directory.
 * For BaseFolder with logging see [LogFile]
 * For BaseFolder with config see [ConfigFile]
 * For Both see [BaseFolderWithLogAndConfig]
 *
 * @param path to your root working directory. Do not use a separator at the and! Defaults to user.home
 * @param name of the workingDirFolder
 * @param printInfo if you don't want to print the information to stdout, set this to false.
 */
open class BaseFolder(path: String = System.getProperty("user.home"), name: String, printInfo: Boolean = true) {

    val baseFolder = File(path + File.separator + name)

    init {
        if (baseFolder.exists()) {
            if (baseFolder.isDirectory.not()) {
                if (baseFolder.mkdirs()) println("Created base folder at ${baseFolder.absolutePath}")
                else println("Could not create base folder at ${baseFolder.absolutePath}", printInfo)
            } else println("Base folder exists at ${baseFolder.absolutePath}", printInfo)
        } else {
            if (baseFolder.mkdirs()) println("Created base folder at ${baseFolder.absolutePath}", printInfo)
            else println("Could not create base folder at ${baseFolder.absolutePath}", printInfo)
        }
    }
}

internal fun println(msg: String, print: Boolean) {
    print.ifTrue { println(msg) }
}