package file

import file.ConfigFile

/**
 * Manages base folder, config file and log file.
 * See [BaseFolder], [LogFile] and [ConfigFile] for more.
 *
 * @param format for the date used in the config file
 * @param allowConfigChanges allows to change the config at runtime through [reloadConfig]
 * @param default config to use for config creation
 * @param loadConfig used to deserialize the config-object from the string of the file to the config object. The second parameter is the default-config-object. I suggest using kotlin JSON serialize.
 * @param storeConfig used to serialize the config-object to a string which will be written to the config file. I suggest using kotlin JSON serialize.
 * @param path to your root working directory. Do not use a separator at the and! Defaults to user.home
 * @param name of the workingDirFolder
 */
class BaseFolderWithLogAndConfig<T>(
    format: String = "dd.MM.yyyy HH:mm",
    allowConfigChanges: Boolean = false,
    default: T,
    loadConfig: (String, T) -> T,
    storeConfig: (T) -> String,
    path: String = System.getProperty("user.home"),
    name: String
) : BaseFolder( path, name) {
    val log = LogFile(format, this)
    val config = ConfigFile(allowConfigChanges, default, loadConfig, storeConfig, this)

}