package kotnexlib

/**
 * Enum class defining the available colors for command line output.
 *
 * Each color is represented by its corresponding ANSI color code, which can be used to format the output text.
 *
 * @property colorCode The ANSI color code for the color.
 *
 * @constructor Creates a new instance of the CommandLineColors enum class with the specified color code.
 */
enum class CommandLineColors(val colorCode: String) {
    BLACK("\u001B[30m"),
    RED("\u001B[31m"),
    GREEN("\u001B[32m"),
    YELLOW("\u001B[33m"),
    BLUE("\u001B[34m"),
    PURPLE("\u001B[35m"),
    CYAN("\u001B[36m"),
    WHITE("\u001B[37m"),

    //Not mentioned to be used as color. Only resets a color!
    RESET("\u001B[0m");

    fun println(text: String) = printlnColored(text, this)
    fun print(text: String) = printColored(text, this)
}

/**
 * Prints the specified text in the given color to the console.
 * This may only work on Linux!
 *
 * @param text The text to be printed.
 * @param color The color to be used for printing the text.
 */
fun printlnColored(text: String, color: CommandLineColors) {
    println("${color.colorCode}$text${CommandLineColors.RESET.colorCode}")
}

/**
 * Prints the specified text in the given color to the console.
 * This may only work on Linux!
 *
 * @param text The text to be printed.
 * @param color The color to be used for printing the text.
 */
fun printColored(text: String, color: CommandLineColors) {
    print("${color.colorCode}$text${CommandLineColors.RESET.colorCode}")
}

