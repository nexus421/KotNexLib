package file

/**
 * A configuration file manager that operates on simple key-value pairs.
 * The configuration is stored as a text file, where each line represents a key-value pair in the format `key=value`.
 *
 * This class extends [ConfigFile] and provides implementation for loading and storing
 * the configuration data as `Map<String, String>`.
 *
 * @param folder The base folder where the configuration file is stored. Defaults to a folder named "Config" in user.home.
 *
 * The configuration is loaded from the file during initialization. If the file does not exist, it is created
 * with an empty default configuration. The configuration can be accessed, reloaded, or updated using the
 * parent class methods.
 */
class KeyValueConfigFile(folder: BaseFolder = BaseFolder(name = "Config")) : ConfigFile<Map<String, String>>(
    default = mapOf(),
    loadConfig = { raw, default ->
        try {
            raw.split("\n").associate { it.split("=").let { parts -> parts[0] to parts[1] } }
        } catch (e: Exception) {
            System.err.println(e.message)
            default
        }
    },
    storeConfig = { it.entries.joinToString("\n") { map -> "${map.key}=${map.value}" } }
)