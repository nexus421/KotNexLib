package file

import ifNull
import java.io.File

/**
 * Manages a simple config file.
 *
 * @param allowConfigChanges allows to change the config at runtime through [reloadConfig]
 * @param default config to use for config creation
 * @param loadConfig used to deserialize the config-object from the string of the file to the config object. The second parameter is the default-config-object. I suggest using kotlin JSON serialize.
 * @param storeConfig used to serialize the config-object to a string which will be written to the config file. I suggest using kotlin JSON serialize.
 * @param baseFolder for storage
 * @param printLoadedConfig if true, the loaded config will be printed to standard output through toString.
 */
open class ConfigFile<T>(
    val allowConfigChanges: Boolean = false,
    default: T,
    private val loadConfig: (String, T) -> T,
    private val storeConfig: (T) -> String,
    baseFolder: BaseFolder? = null,
    printLoadedConfig: Boolean = false
) {

    val configFile = baseFolder?.baseFolder.ifNull(isNull = {
        File("config.json")
    }) {
        File(this, "config.json")
    }
    var config: T
        private set

    init {
        if (configFile.exists()) {
            config = loadConfig(configFile.readText(), default)
            println("Config loaded with ${if (printLoadedConfig) config.toString() else ""}")
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

    /**
     * Reads [configFile] new from memory an refreshes [config]
     *
     * @return new loaded config
     */
    @Synchronized
    fun reloadConfig(): T {
        config = loadConfig(configFile.readText(), config)
        println("Reloaded config from ${configFile.absolutePath}")
        return config
    }

    /**
     * If [allowConfigChanges] is true, you can change the stored config file.
     * Everything from [newConfig] will be written to [configFile] and hold in memory through [config]
     */
    @Synchronized
    fun storeNewConfig(newConfig: T) {
        if (allowConfigChanges) {
            configFile.writeText(storeConfig(newConfig))
            config = newConfig
        } else println("Config changes are not allowed! Change allowConfigChanges to true in constructor to change setting at runtime.")
    }


}