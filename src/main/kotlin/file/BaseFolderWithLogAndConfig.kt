package file

/**
 * Manages base folder, config file and log file.
 * See [BaseFolder], [LogFile] and [ConfigFile] for more.
 *
 * @param format for the date used in the config file
 * @param allowConfigChanges allows to change the config at runtime through [reloadConfig]
 * @param default config to use for config creation
 * @param loadConfig used to deserialize the config-object from the string of the file to the config object. The second parameter is the default-config-object. I suggest using kotlin JSON serialize.
 * @param storeConfig used to serialize the config-object to a string which will be written to the config file. I suggest using kotlin JSON serialize.
 * @param logSizeSettings for custom Log settings
 * @param baseFolder Parent folder for log and config
 * @param printInfo if you don't want to print the information to stdout, set this to false.
 */
open class BaseFolderWithLogAndConfig<T>(
    format: String = "dd.MM.yyyy HH:mm",
    allowConfigChanges: Boolean = false,
    default: T,
    loadConfig: (String, T) -> T,
    storeConfig: (T) -> String,
    logSizeSettings: LogSizeSettings = LogSizeSettings(),
    val baseFolder: BaseFolder,
    printInfo: Boolean = true
) {
    val log =
        LogFile(format = format, baseFolder = baseFolder, logSizeSettings = logSizeSettings, printInfo = printInfo)
    val configFile = ConfigFile(allowConfigChanges, default, loadConfig, storeConfig, baseFolder, printInfo = printInfo)

    fun getConfig() = configFile.config

}