package bayern.kickner.kotlin_extensions_android.file

import java.io.File

/**
 * Manages a simple config file.
 *
 * @param allowConfigChanges allows to change the config at runtime through [reloadConfig]
 * @param default config to use for config creation
 * @param loadConfig used to deserialize the config-object from the string of the file to the config object. The second parameter is the default-config-object. I suggest using kotlin JSON serialize.
 * @param storeConfig used to serialize the config-object to a string which will be written to the config file. I suggest using kotlin JSON serialize.
 * @param baseFolder for storage
 */
open class ConfigFile<T>(
    val allowConfigChanges: Boolean = false,
    default: T,
    private val loadConfig: (String, T) -> T,
    private val storeConfig: (T) -> String,
    baseFolder: BaseFolder
) {

    val configFile = File(baseFolder.baseFolder, "config.json")
    var config: T
        private set

    init {
        if (configFile.exists()) {
            config = loadConfig(configFile.readText(), default)
        } else {
            if (configFile.createNewFile()) {
                configFile.writeText(storeConfig(default))
                config = default
                println("Config created at ${configFile.absolutePath} with the default object.")
            } else {
                config = default
                println("Error creating config file at ${configFile.absolutePath}! Only the default config is available!")
            }
        }
    }

    @Synchronized
    fun reloadConfig(): T {
        config = loadConfig(configFile.readText(), config)
        println("Reloaded config from ${configFile.absolutePath}")
        return config
    }

    @Synchronized
    fun storeNewConfig(newConfig: T) {
        if (allowConfigChanges) {
            configFile.writeText(storeConfig(newConfig))
            config = newConfig
        } else println("Config changes are not allowed! Change allowConfigChanges to true in constructor to change setting at runtime.")
    }


}