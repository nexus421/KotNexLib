package kotnexlib

/**
 * Simple class to handle ARGS Arguments.
 *
 * Use this class to get specific values from a key from your args.
 *
 * @param args from the main function
 * @param splittingChar key-value-pairs will be separated by this char. Example: key=value
 *
 * Hint: ARGS will be splitted by whitespaces! If you want to prevent a string to be splitted, place it within quotation marks.
 * Examples with default parameters:
 * test=Usher --> Key = "test", value = "Usher"
 * test=Usher is cool! --> Key = "test", value = "Usher" --> "is" and "cool!" are another arguments within [args]!
 * test="Usher is cool!" --> Key = "test", value = "Usher is cool!"
 * test=123 -> Key = "test", value = "123"
 */
class ArgsInterpreter(private val args: Array<String>, private val splittingChar: Char = '=') {

    /**
     * Gets any value, found by this [key] and returns it as a string.
     * If multiple keys are contained, only the first will be returned.
     *
     * @param key the key to search the args for.
     *
     * @return null if the key was not found or the found string value
     */
    fun getValue(key: String) =
        args.find { it.split(splittingChar).firstOrNull() == key }?.split(splittingChar)?.getOrNull(1)

    /**
     * Gets any value, found by this [key] and try to cast it to Int.
     * If multiple keys are contained, only the first will be returned
     *
     * @return the found value as Int. Null if the key was not found or the conversion to int did not work.
     */
    fun getValueAsInt(key: String) = getValue(key)?.toIntOrNull()

    /**
     * Gets any value, found by this [key] and try to cast it to Double.
     * If multiple keys are contained, only the first will be returned
     *
     * @return the found value as double. Null if the key was not found or the conversion to double did not work.
     */
    fun getValueAsDouble(key: String) = getValue(key)?.toDoubleOrNull()

    /**
     * Gets any value, found by this [key] and try to cast it to Boolean.
     * If multiple keys are contained, only the first will be returned
     *
     * @return the found value as boolean. Null if the key was not found or the conversion to boolean did not work.
     */
    fun getValueAsBoolean(key: String) = getValue(key)?.toBooleanStrictOrNull()

    /**
     * Searches for "parameters" without any value.
     * Example: "-h" or "-help"
     *
     * Hint: The "-" will automatically be appended at the beginning of [name]
     *
     * @param name of the parameter without the "-".
     * @return true if the parameter was found, false otherwise.
     */
    fun containsParam(name: String) = args.contains("-$name")

}