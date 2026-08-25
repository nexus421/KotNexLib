package kotnexlib.terminal

import kotnexlib.CommandLineColors
import kotnexlib.printColored
import kotnexlib.printlnColored

/**
 * Interactive terminal prompts (stdin). Uses the same [CommandLineColors] formatting and
 * `NO_COLOR` detection as `kotnexlib.ColoredPrinters`, since it calls `printColored`/`printlnColored` internally.
 */
object Prompts {

    /**
     * Reads a line of text input from stdin.
     *
     * @param question the question to display.
     * @param default optional default value returned when the input is empty.
     * @return the entered string, or [default] if the input was empty. Never `null`.
     */
    fun prompt(question: String, default: String? = null): String {
        val defaultHint = if (default != null) " [$default]" else ""
        printColored("$question$defaultHint: ", CommandLineColors.CYAN)
        val input = readlnOrNull()?.trim()
        return if (input.isNullOrEmpty() && default != null) default else (input ?: "")
    }

    /**
     * Reads a yes/no confirmation. Any input other than `y`/`yes`/`n`/`no` (case-insensitive) falls
     * back to [default], including when stdin is closed (`readln` returns `null`).
     *
     * @param question the question to display.
     * @param default the value used when the input is empty or unrecognized.
     * @return `true` for yes, `false` for no.
     */
    fun confirm(question: String, default: Boolean = true): Boolean {
        val options = if (default) "[Y/n]" else "[y/N]"
        printColored("$question $options: ", CommandLineColors.YELLOW)
        val input = readlnOrNull()?.trim()?.lowercase()
        return when (input) {
            "y", "yes" -> true
            "n", "no" -> false
            else -> default
        }
    }

    /**
     * Offers a numbered selection list and re-prompts until a valid choice is made.
     *
     * @param title the heading shown above the selection list.
     * @param options the values to choose from. Must not be empty.
     * @param labelMapper converts an element of [options] into its display label.
     * @return the selected element from [options].
     * @throws IllegalStateException if stdin is closed/exhausted before a valid choice is made
     * (e.g. a non-interactive process with no more input) — this stops the loop instead of spinning
     * forever re-prompting against a stream that will never produce more input.
     */
    fun <T> select(title: String, options: List<T>, labelMapper: (T) -> String = { it.toString() }): T {
        require(options.isNotEmpty()) { "options must not be empty!" }
        printlnColored(title, CommandLineColors.CYAN)
        options.forEachIndexed { index, option ->
            println("  ${index + 1}) ${labelMapper(option)}")
        }
        while (true) {
            printColored("Enter choice (1-${options.size}): ", CommandLineColors.CYAN)
            val input = readlnOrNull()
                ?: throw IllegalStateException("Input stream closed before a valid choice was made.")
            val selectedIndex = input.trim().toIntOrNull()?.minus(1)
            if (selectedIndex != null && selectedIndex in options.indices) {
                return options[selectedIndex]
            }
            printlnColored("Invalid choice, please try again.", CommandLineColors.RED)
        }
    }
}
