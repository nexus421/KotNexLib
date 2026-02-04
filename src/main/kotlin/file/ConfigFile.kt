package file

import kotnexlib.ifNull
import java.io.File

/**
 * Manages a simple config file. Load, edit and store will be managed.
 *
 * Example:
 * ConfigFile(
 *     default = TestConfig(""), loadConfig = { raw, default ->
 *         try {
 *             TODO("Convert raw to TestConfig. May use JSON with Kotlinx.serialisation for example.")
 *         } catch (e: Exception) {
 *             default
 *         }
 *     },
 *     storeConfig = { testConfig ->
 *         //Serialize this class to any String-representation. JSON for example.
 *         testConfig.toString()
 *     },
 *     baseFolder = BaseFolder(name = "Test")
 * )
 *
 * @param allowConfigChanges allows to change the config at runtime through [storeNewConfig]. Otherwise, only an error will be printed.
 * @param default config to use for config creation
 * @param loadConfig used to deserialize the config-object from the string of the file to the config object. The second parameter is the default-config-object. I suggest using kotlin JSON serialize.
 * @param storeConfig used to serialize the config-object to a string which will be written to the config file. I suggest using kotlin JSON serialize.
 * @param baseFolder as parent for config storage
 * @param printLoadedConfig if true, the loaded config will be printed to standard output through toString.
 * @param printInfo if you don't want to print the information to stdout, set this to false. Overrides printLoadedConfig
 */
open class ConfigFile<T>(
    val allowConfigChanges: Boolean = false,
    default: T,
    private val loadConfig: (String, T) -> T,
    private val storeConfig: (T) -> String,
    baseFolder: BaseFolder? = null,
    printLoadedConfig: Boolean = false,
    private val printInfo: Boolean = true
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
            println("Config loaded with ${if (printLoadedConfig) config.toString() else ""}", printInfo)
        } else {
            if (configFile.createNewFile()) {
                configFile.writeText(storeConfig(default))
                config = default
                println("Config created at ${configFile.absolutePath} with the default object.", printInfo)
            } else {
                config = default
                println(
                    "Error creating config file at ${configFile.absolutePath}! Only the default config is available!",
                    printInfo
                )
            }
        }
    }

    /**
     * Reads [configFile] new from storage into memory an refreshes [config]
     *
     * @return new loaded config
     */
    @Synchronized
    fun reloadConfig(): T {
        config = loadConfig(configFile.readText(), config)
        println("Reloaded config from ${configFile.absolutePath}", printInfo)
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
        } else println(
            "Config changes are not allowed! Change allowConfigChanges to true in constructor to change setting at runtime.",
            printInfo
        )
    }
}

