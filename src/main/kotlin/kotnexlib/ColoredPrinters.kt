package kotnexlib

import enums.AsciiNP
import kotlin.math.max
import kotlin.math.min

/*
Example:
println("ColoredPrinters Example")
    println("======================")
    println()

    // Platform detection
    println("Platform detection:")
    println("Running on Linux: ${Terminal.isLinux()}")
    println("Running on macOS: ${Terminal.isMacOS()}")
    println("Running on Windows: ${Terminal.isWindows()}")
    println("Supports ANSI colors: ${Terminal.supportsAnsiColors()}")
    println()

    // Basic colored output
    println("Basic colored output:")
    CommandLineColors.RED.println("This text is red")
    CommandLineColors.GREEN.println("This text is green")
    CommandLineColors.BLUE.println("This text is blue")
    CommandLineColors.YELLOW.println("This text is yellow")
    println()

    // Background colors
    println("Background colors:")
    printlnWithBackground("This text has a red background", CommandLineBackgroundColors.RED)
    printlnWithBackground("This text has a green background", CommandLineBackgroundColors.GREEN)
    printlnWithBackground("This text has a blue background", CommandLineBackgroundColors.BLUE)
    printlnWithBackground("This text has a yellow background", CommandLineBackgroundColors.YELLOW)
    println()

    // Text styles
    println("Text styles:")
    printlnWithStyle("This text is bold", CommandLineStyles.BOLD)
    printlnWithStyle("This text is italic", CommandLineStyles.ITALIC)
    printlnWithStyle("This text is underlined", CommandLineStyles.UNDERLINE)
    printlnWithStyle("This text is blinking", CommandLineStyles.BLINK)
    println()

    // Combined formatting
    println("Combined formatting:")
    printlnFormatted(
        "This text is red, on a white background, and bold",
        CommandLineColors.RED,
        CommandLineBackgroundColors.WHITE,
        CommandLineStyles.BOLD
    )
    println()

    // Progress bar
    println("Progress bar:")
    val progressBar = ProgressBar(
        width = 40,
        prefix = "Loading: ",
        suffix = "",
        color = CommandLineColors.CYAN
    )

    for (i in 0..100) {
        progressBar.update(i.toDouble() / 100.0)
        Thread.sleep(20)
    }
    progressBar.complete()
    println()

    // Spinner
    println("Spinner:")
    val spinner = createSpinner("Loading data", CommandLineColors.GREEN)
    spinner.start()
    Thread.sleep(3000)
    spinner.stop("Data loaded successfully!")
    println()

    // Table
    println("Table:")
    val table = Table(
        headers = listOf("Name", "Age", "City"),
        columnColors = listOf(CommandLineColors.CYAN, CommandLineColors.YELLOW, CommandLineColors.GREEN),
        headerColor = CommandLineColors.BRIGHT_WHITE,
        borderColor = CommandLineColors.BRIGHT_BLUE
    )
    table.addRow("John", "25", "New York")
    table.addRow("Alice", "30", "London")
    table.addRow("Bob", "22", "Paris")
    table.print()
    println()

    // Updating a line in-place
    println("Updating a line in-place:")
    println("Watch the line below change:")
    for (i in 0..100 step 10) {
        updateLine("Processing: $i%", CommandLineColors.PURPLE)
        Thread.sleep(200)
    }
    updateLine("Processing: Complete!", CommandLineColors.GREEN)
    println("\n")

    // Loading indicator
    println("Loading indicator:")
    val loadingIndicator = createLoadingIndicator("Downloading:", CommandLineColors.BRIGHT_CYAN)
    for (i in 0..100 step 5) {
        loadingIndicator(i.toDouble() / 100.0)
        Thread.sleep(100)
    }
    println()

    // Cursor manipulation
    println("Cursor manipulation:")
    println("Line 1")
    println("Line 2")
    println("Line 3")
    Cursor.up(2)
    printColored("Modified Line 2", CommandLineColors.RED)
    println()
    Cursor.down(1)
    printColored("Modified Line 3", CommandLineColors.GREEN)
    println("\n")

    println("Example complete!")
 */

/**
 * Utility object for terminal operations.
 * Provides platform detection and terminal capabilities.
 */
object Terminal {
    /**
     * Checks if the current platform is Linux.
     * @return true if the current platform is Linux, false otherwise.
     */
    fun isLinux(): Boolean = SystemProperties.osName().lowercase().contains("linux")

    /**
     * Checks if the current platform is macOS.
     * @return true if the current platform is macOS, false otherwise.
     */
    fun isMacOS(): Boolean = SystemProperties.osName().lowercase().contains("mac")

    /**
     * Checks if the current platform is Windows.
     * @return true if the current platform is Windows, false otherwise.
     */
    fun isWindows(): Boolean = SystemProperties.osName().lowercase().contains("windows")

    /**
     * Checks if ANSI color codes are supported on the current platform.
     * @return true if ANSI color codes are supported, false otherwise.
     */
    fun supportsAnsiColors(): Boolean = isLinux() || isMacOS() || (isWindows() && System.getenv("TERM") != null)

    /**
     * The escape character used for ANSI escape sequences.
     */
    val ESC: String = AsciiNP.ESC.asText

    /**
     * The control sequence introducer for ANSI escape sequences.
     */
    val CSI: String = "$ESC["
}

/**
 * Enum class defining the available foreground colors for command line output.
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

    // Bright colors
    BRIGHT_BLACK("\u001B[90m"),
    BRIGHT_RED("\u001B[91m"),
    BRIGHT_GREEN("\u001B[92m"),
    BRIGHT_YELLOW("\u001B[93m"),
    BRIGHT_BLUE("\u001B[94m"),
    BRIGHT_PURPLE("\u001B[95m"),
    BRIGHT_CYAN("\u001B[96m"),
    BRIGHT_WHITE("\u001B[97m"),

    //Not mentioned to be used as color. Only resets a color!
    RESET("\u001B[0m");

    fun println(text: String) = printlnColored(text, this)
    fun print(text: String) = printColored(text, this)
}

/**
 * Enum class defining the available background colors for command line output.
 *
 * Each color is represented by its corresponding ANSI color code, which can be used to format the output text background.
 *
 * @property colorCode The ANSI color code for the background color.
 *
 * @constructor Creates a new instance of the CommandLineBackgroundColors enum class with the specified color code.
 */
enum class CommandLineBackgroundColors(val colorCode: String) {
    BLACK("\u001B[40m"),
    RED("\u001B[41m"),
    GREEN("\u001B[42m"),
    YELLOW("\u001B[43m"),
    BLUE("\u001B[44m"),
    PURPLE("\u001B[45m"),
    CYAN("\u001B[46m"),
    WHITE("\u001B[47m"),

    // Bright background colors
    BRIGHT_BLACK("\u001B[100m"),
    BRIGHT_RED("\u001B[101m"),
    BRIGHT_GREEN("\u001B[102m"),
    BRIGHT_YELLOW("\u001B[103m"),
    BRIGHT_BLUE("\u001B[104m"),
    BRIGHT_PURPLE("\u001B[105m"),
    BRIGHT_CYAN("\u001B[106m"),
    BRIGHT_WHITE("\u001B[107m"),

    //Not mentioned to be used as color. Only resets a color!
    RESET("\u001B[0m");
}

/**
 * Enum class defining the available text styles for command line output.
 *
 * Each style is represented by its corresponding ANSI style code, which can be used to format the output text.
 *
 * @property styleCode The ANSI style code for the text style.
 *
 * @constructor Creates a new instance of the CommandLineStyles enum class with the specified style code.
 */
enum class CommandLineStyles(val styleCode: String) {
    BOLD("\u001B[1m"),
    ITALIC("\u001B[3m"),
    UNDERLINE("\u001B[4m"),
    BLINK("\u001B[5m"),
    REVERSE("\u001B[7m"),
    HIDDEN("\u001B[8m"),
    STRIKETHROUGH("\u001B[9m"),

    //Not mentioned to be used as style. Only resets a style!
    RESET_BOLD("\u001B[22m"),
    RESET_ITALIC("\u001B[23m"),
    RESET_UNDERLINE("\u001B[24m"),
    RESET_BLINK("\u001B[25m"),
    RESET_REVERSE("\u001B[27m"),
    RESET_HIDDEN("\u001B[28m"),
    RESET_STRIKETHROUGH("\u001B[29m"),
    RESET_ALL("\u001B[0m");
}

/**
 * Prints the specified text in the given color to the console.
 * This function works on Linux, macOS, and may work on Windows depending on the terminal.
 *
 * @param text The text to be printed.
 * @param color The color to be used for printing the text.
 */
fun printlnColored(text: String, color: CommandLineColors) {
    if (Terminal.supportsAnsiColors()) {
        println("${color.colorCode}$text${CommandLineColors.RESET.colorCode}")
    } else {
        println(text)
    }
}

/**
 * Prints the specified text in the given color to the console.
 * This function works on Linux, macOS, and may work on Windows depending on the terminal.
 *
 * @param text The text to be printed.
 * @param color The color to be used for printing the text.
 */
fun printColored(text: String, color: CommandLineColors) {
    if (Terminal.supportsAnsiColors()) {
        print("${color.colorCode}$text${CommandLineColors.RESET.colorCode}")
    } else {
        print(text)
    }
}

/**
 * Prints the specified text with the given background color to the console.
 * This function works on Linux, macOS, and may work on Windows depending on the terminal.
 *
 * @param text The text to be printed.
 * @param backgroundColor The background color to be used for printing the text.
 */
fun printlnWithBackground(text: String, backgroundColor: CommandLineBackgroundColors) {
    if (Terminal.supportsAnsiColors()) {
        println("${backgroundColor.colorCode}$text${CommandLineBackgroundColors.RESET.colorCode}")
    } else {
        println(text)
    }
}

/**
 * Prints the specified text with the given background color to the console.
 * This function works on Linux, macOS, and may work on Windows depending on the terminal.
 *
 * @param text The text to be printed.
 * @param backgroundColor The background color to be used for printing the text.
 */
fun printWithBackground(text: String, backgroundColor: CommandLineBackgroundColors) {
    if (Terminal.supportsAnsiColors()) {
        print("${backgroundColor.colorCode}$text${CommandLineBackgroundColors.RESET.colorCode}")
    } else {
        print(text)
    }
}

/**
 * Prints the specified text with the given style to the console.
 * This function works on Linux, macOS, and may work on Windows depending on the terminal.
 *
 * @param text The text to be printed.
 * @param style The style to be used for printing the text.
 */
fun printlnWithStyle(text: String, style: CommandLineStyles) {
    if (Terminal.supportsAnsiColors()) {
        println("${style.styleCode}$text${CommandLineStyles.RESET_ALL.styleCode}")
    } else {
        println(text)
    }
}

/**
 * Prints the specified text with the given style to the console.
 * This function works on Linux, macOS, and may work on Windows depending on the terminal.
 *
 * @param text The text to be printed.
 * @param style The style to be used for printing the text.
 */
fun printWithStyle(text: String, style: CommandLineStyles) {
    if (Terminal.supportsAnsiColors()) {
        print("${style.styleCode}$text${CommandLineStyles.RESET_ALL.styleCode}")
    } else {
        print(text)
    }
}

/**
 * Prints the specified text with the given color, background color, and style to the console.
 * This function works on Linux, macOS, and may work on Windows depending on the terminal.
 *
 * @param text The text to be printed.
 * @param color The color to be used for printing the text.
 * @param backgroundColor The background color to be used for printing the text.
 * @param style The style to be used for printing the text.
 */
fun printlnFormatted(
    text: String,
    color: CommandLineColors? = null,
    backgroundColor: CommandLineBackgroundColors? = null,
    style: CommandLineStyles? = null
) {
    if (Terminal.supportsAnsiColors()) {
        val colorCode = color?.colorCode ?: ""
        val bgColorCode = backgroundColor?.colorCode ?: ""
        val styleCode = style?.styleCode ?: ""
        println("$colorCode$bgColorCode$styleCode$text${CommandLineColors.RESET.colorCode}")
    } else {
        println(text)
    }
}

/**
 * Prints the specified text with the given color, background color, and style to the console.
 * This function works on Linux, macOS, and may work on Windows depending on the terminal.
 *
 * @param text The text to be printed.
 * @param color The color to be used for printing the text.
 * @param backgroundColor The background color to be used for printing the text.
 * @param style The style to be used for printing the text.
 */
fun printFormatted(
    text: String,
    color: CommandLineColors? = null,
    backgroundColor: CommandLineBackgroundColors? = null,
    style: CommandLineStyles? = null
) {
    if (Terminal.supportsAnsiColors()) {
        val colorCode = color?.colorCode ?: ""
        val bgColorCode = backgroundColor?.colorCode ?: ""
        val styleCode = style?.styleCode ?: ""
        print("$colorCode$bgColorCode$styleCode$text${CommandLineColors.RESET.colorCode}")
    } else {
        print(text)
    }
}

/**
 * Cursor manipulation functions for terminal control.
 */
object Cursor {
    /**
     * Moves the cursor up by the specified number of lines.
     * @param lines The number of lines to move up.
     */
    fun up(lines: Int = 1) {
        if (Terminal.supportsAnsiColors()) {
            print("${Terminal.CSI}${lines}A")
        }
    }

    /**
     * Moves the cursor down by the specified number of lines.
     * @param lines The number of lines to move down.
     */
    fun down(lines: Int = 1) {
        if (Terminal.supportsAnsiColors()) {
            print("${Terminal.CSI}${lines}B")
        }
    }

    /**
     * Moves the cursor right by the specified number of columns.
     * @param columns The number of columns to move right.
     */
    fun right(columns: Int = 1) {
        if (Terminal.supportsAnsiColors()) {
            print("${Terminal.CSI}${columns}C")
        }
    }

    /**
     * Moves the cursor left by the specified number of columns.
     * @param columns The number of columns to move left.
     */
    fun left(columns: Int = 1) {
        if (Terminal.supportsAnsiColors()) {
            print("${Terminal.CSI}${columns}D")
        }
    }

    /**
     * Moves the cursor to the beginning of the line.
     */
    fun moveToStartOfLine() {
        if (Terminal.supportsAnsiColors()) {
            print("\r")
        }
    }

    /**
     * Clears the current line.
     */
    fun clearLine() {
        if (Terminal.supportsAnsiColors()) {
            print("${Terminal.CSI}2K")
            moveToStartOfLine()
        }
    }

    /**
     * Clears the screen and moves the cursor to the home position (0, 0).
     */
    fun clearScreen() {
        if (Terminal.supportsAnsiColors()) {
            print("${Terminal.CSI}2J${Terminal.CSI}H")
        }
    }

    /**
     * Saves the current cursor position.
     */
    fun savePosition() {
        if (Terminal.supportsAnsiColors()) {
            print("${Terminal.CSI}s")
        }
    }

    /**
     * Restores the cursor to the last saved position.
     */
    fun restorePosition() {
        if (Terminal.supportsAnsiColors()) {
            print("${Terminal.CSI}u")
        }
    }

    /**
     * Hides the cursor.
     */
    fun hide() {
        if (Terminal.supportsAnsiColors()) {
            print("${Terminal.CSI}?25l")
        }
    }

    /**
     * Shows the cursor.
     */
    fun show() {
        if (Terminal.supportsAnsiColors()) {
            print("${Terminal.CSI}?25h")
        }
    }
}

/**
 * Progress bar implementation for displaying progress in the terminal.
 *
 * @property width The width of the progress bar in characters.
 * @property charFull The character used to represent a filled portion of the progress bar.
 * @property charEmpty The character used to represent an empty portion of the progress bar.
 * @property prefix The prefix text displayed before the progress bar.
 * @property suffix The suffix text displayed after the progress bar.
 * @property color The color of the progress bar.
 * @property showPercentage Whether to show the percentage in the suffix.
 */
class ProgressBar(
    private val width: Int = 50,
    private val charFull: Char = '█',
    private val charEmpty: Char = '░',
    private val prefix: String = "Progress: ",
    private val suffix: String = "",
    private val color: CommandLineColors = CommandLineColors.GREEN,
    private val showPercentage: Boolean = true
) {
    private var progress: Double = 0.0
    private var lastPrintedLength: Int = 0

    /**
     * Updates the progress bar with the specified progress value.
     * @param progress The progress value between 0.0 and 1.0.
     */
    fun update(progress: Double) {
        this.progress = progress.coerceIn(0.0, 1.0)
        render()
    }

    /**
     * Updates the progress bar with the specified progress value.
     * @param current The current progress value.
     * @param total The total progress value.
     */
    fun update(current: Int, total: Int) {
        update(current.toDouble() / total.toDouble())
    }

    /**
     * Renders the progress bar to the terminal.
     */
    private fun render() {
        if (!Terminal.supportsAnsiColors()) {
            return
        }

        val filledWidth = (width * progress).toInt()
        val emptyWidth = width - filledWidth

        val progressBar = charFull.toString().repeat(filledWidth) + charEmpty.toString().repeat(emptyWidth)
        val percentage = if (showPercentage) " ${(progress * 100).toInt()}%" else ""
        val line = "$prefix$progressBar$suffix$percentage"

        Cursor.clearLine()
        printColored(line, color)
        System.out.flush()
        lastPrintedLength = line.length
    }

    /**
     * Completes the progress bar and moves to the next line.
     */
    fun complete() {
        update(1.0)
        println()
    }
}

/**
 * Spinner implementation for displaying an animation during long-running tasks.
 *
 * @property frames The frames of the spinner animation.
 * @property delay The delay between frames in milliseconds.
 * @property prefix The prefix text displayed before the spinner.
 * @property suffix The suffix text displayed after the spinner.
 * @property color The color of the spinner.
 */
class Spinner(
    private val frames: Array<String> = arrayOf("⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"),
    private val delay: Long = 100,
    private val prefix: String = "",
    private val suffix: String = "",
    private val color: CommandLineColors = CommandLineColors.CYAN
) {
    private var isSpinning = false
    private var currentFrame = 0
    private var thread: Thread? = null

    /**
     * Starts the spinner animation.
     * @param message The message to display alongside the spinner.
     */
    fun start(message: String = "") {
        if (!Terminal.supportsAnsiColors()) {
            println("$prefix$message$suffix")
            return
        }

        isSpinning = true
        Cursor.hide()

        thread = Thread {
            try {
                while (isSpinning) {
                    render(message)
                    Thread.sleep(delay)
                    currentFrame = (currentFrame + 1) % frames.size
                }
            } catch (e: InterruptedException) {
                // Thread was interrupted, stop spinning
                isSpinning = false
            } finally {
                Cursor.show()
            }
        }

        thread?.start()
    }

    /**
     * Renders the current frame of the spinner animation.
     * @param message The message to display alongside the spinner.
     */
    private fun render(message: String) {
        Cursor.clearLine()
        printColored("$prefix${frames[currentFrame]} $message$suffix", color)
        System.out.flush()
    }

    /**
     * Stops the spinner animation.
     * @param finalMessage The final message to display after stopping the spinner.
     */
    fun stop(finalMessage: String? = null) {
        isSpinning = false
        thread?.join()

        if (Terminal.supportsAnsiColors()) {
            Cursor.clearLine()
            if (finalMessage != null) {
                println(finalMessage)
            }
            Cursor.show()
        } else if (finalMessage != null) {
            println(finalMessage)
        }
    }
}

/**
 * Table implementation for displaying tabular data in the terminal.
 *
 * @property headers The headers of the table.
 * @property columnColors The colors for each column.
 * @property headerColor The color of the headers.
 * @property borderColor The color of the table borders.
 * @property borderStyle The style of the table borders.
 */
class Table(
    private val headers: List<String>,
    private val columnColors: List<CommandLineColors> = List(headers.size) { CommandLineColors.WHITE },
    private val headerColor: CommandLineColors = CommandLineColors.BRIGHT_WHITE,
    private val borderColor: CommandLineColors = CommandLineColors.WHITE,
    private val borderStyle: CommandLineStyles = CommandLineStyles.BOLD
) {
    private val rows = mutableListOf<List<String>>()
    private val columnWidths = headers.map { it.length }.toMutableList()

    /**
     * Adds a row to the table.
     * @param row The row to add.
     */
    fun addRow(row: List<String>) {
        require(row.size == headers.size) { "Row size must match headers size" }
        rows.add(row)

        // Update column widths
        row.forEachIndexed { index, cell ->
            columnWidths[index] = max(columnWidths[index], cell.length)
        }
    }

    /**
     * Adds a row to the table.
     * @param vararg cells The cells of the row.
     */
    fun addRow(vararg cells: String) {
        addRow(cells.toList())
    }

    /**
     * Prints the table to the terminal.
     */
    fun print() {
        if (!Terminal.supportsAnsiColors()) {
            printPlainTable()
            return
        }

        printBorder(true)
        printHeaders()
        printBorder(false)

        rows.forEach { row ->
            printRow(row)
        }

        printBorder(true)
    }

    /**
     * Prints a plain text version of the table (without colors or styles).
     */
    private fun printPlainTable() {
        val horizontalBorder = "+${columnWidths.map { "-".repeat(it + 2) }.joinToString("+")}+"

        println(horizontalBorder)
        println("| ${headers.mapIndexed { i, header -> header.padEnd(columnWidths[i]) }.joinToString(" | ")} |")
        println(horizontalBorder)

        rows.forEach { row ->
            println("| ${row.mapIndexed { i, cell -> cell.padEnd(columnWidths[i]) }.joinToString(" | ")} |")
        }

        println(horizontalBorder)
    }

    /**
     * Prints a border row of the table.
     * @param isDoubleLine Whether to use double lines for the border.
     */
    private fun printBorder(isDoubleLine: Boolean) {
        val horizontal = if (isDoubleLine) "═" else "─"
        val vertical = if (isDoubleLine) "║" else "│"
        val leftCorner = if (isDoubleLine) "╔" else "┌"
        val rightCorner = if (isDoubleLine) "╗" else "┐"
        val middleTop = if (isDoubleLine) "╦" else "┬"
        val leftMiddle = if (isDoubleLine) "╠" else "├"
        val rightMiddle = if (isDoubleLine) "╣" else "┤"
        val middleMiddle = if (isDoubleLine) "╬" else "┼"
        val leftBottom = if (isDoubleLine) "╚" else "└"
        val rightBottom = if (isDoubleLine) "╝" else "┘"
        val middleBottom = if (isDoubleLine) "╩" else "┴"

        val border = StringBuilder()

        if (isDoubleLine) {
            border.append(leftCorner)
            columnWidths.forEachIndexed { index, width ->
                border.append(horizontal.repeat(width + 2))
                if (index < columnWidths.size - 1) {
                    border.append(middleTop)
                }
            }
            border.append(rightCorner)
        } else {
            border.append(leftMiddle)
            columnWidths.forEachIndexed { index, width ->
                border.append(horizontal.repeat(width + 2))
                if (index < columnWidths.size - 1) {
                    border.append(middleMiddle)
                }
            }
            border.append(rightMiddle)
        }

        printFormatted(border.toString(), borderColor, style = borderStyle)
        println()
    }

    /**
     * Prints the headers of the table.
     */
    private fun printHeaders() {
        val headerRow = StringBuilder()
        headerRow.append("║ ")

        headers.forEachIndexed { index, header ->
            val paddedHeader = header.padEnd(columnWidths[index])
            headerRow.append(paddedHeader)
            if (index < headers.size - 1) {
                headerRow.append(" │ ")
            }
        }

        headerRow.append(" ║")

        printFormatted(headerRow.toString(), headerColor, style = CommandLineStyles.BOLD)
        println()
    }

    /**
     * Prints a row of the table.
     * @param row The row to print.
     */
    private fun printRow(row: List<String>) {
        print("║ ")

        row.forEachIndexed { index, cell ->
            val paddedCell = cell.padEnd(columnWidths[index])
            printColored(paddedCell, columnColors[min(index, columnColors.size - 1)])

            if (index < row.size - 1) {
                print(" │ ")
            }
        }

        println(" ║")
    }
}

/**
 * Updates a line of text in-place, useful for creating dynamic displays like loading indicators.
 *
 * @param text The new text to display.
 * @param color The color to use for the text.
 */
fun updateLine(text: String, color: CommandLineColors = CommandLineColors.WHITE) {
    if (Terminal.supportsAnsiColors()) {
        Cursor.clearLine()
        printColored(text, color)
        System.out.flush()
    } else {
        println(text)
    }
}

/**
 * Creates a simple loading indicator that can be updated in-place.
 *
 * @param message The message to display alongside the loading indicator.
 * @param color The color to use for the loading indicator.
 * @return A function that can be called to update the loading progress (0.0 to 1.0).
 */
fun createLoadingIndicator(message: String, color: CommandLineColors = CommandLineColors.CYAN): (Double) -> Unit {
    val progressBar = ProgressBar(
        prefix = "$message ",
        color = color
    )

    return { progress ->
        progressBar.update(progress)
        if (progress >= 1.0) {
            println()
        }
    }
}

/**
 * Creates a spinner animation for indeterminate progress.
 *
 * @param message The message to display alongside the spinner.
 * @param color The color to use for the spinner.
 * @return A Spinner object that can be controlled.
 */
fun createSpinner(message: String, color: CommandLineColors = CommandLineColors.CYAN): Spinner {
    return Spinner(prefix = "", suffix = " $message", color = color)
}

/**
 * Attempts to detect the width of the terminal.
 * Falls back to a default width if detection fails.
 *
 * @param defaultWidth The default width to use if detection fails.
 * @return The detected terminal width or the default width.
 */
fun getTerminalWidth(defaultWidth: Int = 80): Int {
    return tryOrNull {
        val process = ProcessBuilder("tput", "cols").redirectErrorStream(true).start()
        val output = process.inputStream.bufferedReader().readText().trim()
        process.waitFor()
        output.toInt()
    } ?: defaultWidth
}

/**
 * Creates a rainbow-colored text by applying different colors to each character.
 *
 * @param text The text to colorize.
 * @param colors The colors to use for the rainbow effect. Defaults to a standard rainbow.
 * @return The rainbow-colored text.
 */
fun createRainbowText(
    text: String,
    colors: List<CommandLineColors> = listOf(
        CommandLineColors.RED,
        CommandLineColors.YELLOW,
        CommandLineColors.GREEN,
        CommandLineColors.CYAN,
        CommandLineColors.BLUE,
        CommandLineColors.PURPLE
    )
): String {
    if (!Terminal.supportsAnsiColors() || colors.isEmpty()) {
        return text
    }

    val result = StringBuilder()
    text.forEachIndexed { index, char ->
        val color = colors[index % colors.size]
        result.append("${color.colorCode}$char")
    }
    result.append(CommandLineColors.RESET.colorCode)
    return result.toString()
}

/**
 * Prints rainbow-colored text to the console.
 *
 * @param text The text to print in rainbow colors.
 * @param colors The colors to use for the rainbow effect. Defaults to a standard rainbow.
 */
fun printRainbow(
    text: String,
    colors: List<CommandLineColors> = listOf(
        CommandLineColors.RED,
        CommandLineColors.YELLOW,
        CommandLineColors.GREEN,
        CommandLineColors.CYAN,
        CommandLineColors.BLUE,
        CommandLineColors.PURPLE
    )
) {
    print(createRainbowText(text, colors))
}

/**
 * Prints rainbow-colored text to the console followed by a newline.
 *
 * @param text The text to print in rainbow colors.
 * @param colors The colors to use for the rainbow effect. Defaults to a standard rainbow.
 */
fun printlnRainbow(
    text: String,
    colors: List<CommandLineColors> = listOf(
        CommandLineColors.RED,
        CommandLineColors.YELLOW,
        CommandLineColors.GREEN,
        CommandLineColors.CYAN,
        CommandLineColors.BLUE,
        CommandLineColors.PURPLE
    )
) {
    println(createRainbowText(text, colors))
}

/**
 * Creates a box around the given text.
 *
 * @param text The text to put in the box.
 * @param padding The padding around the text.
 * @param color The color of the box.
 * @param style The style of the box.
 * @param title Optional title for the box.
 * @return The text with a box around it.
 */
fun createTextBox(
    text: String,
    padding: Int = 1,
    color: CommandLineColors = CommandLineColors.WHITE,
    style: CommandLineStyles = CommandLineStyles.BOLD,
    title: String? = null
): String {
    if (!Terminal.supportsAnsiColors()) {
        return text
    }

    val lines = text.split("\n")
    val maxLineLength = lines.maxOfOrNull { it.length } ?: 0
    val titleLength = title?.length ?: 0
    val boxWidth = maxLineLength + padding * 2
    val actualWidth = maxOf(boxWidth, titleLength + 4)  // Ensure box is wide enough for title

    val horizontalBorder = "─".repeat(actualWidth)
    val emptyLine = "│" + " ".repeat(actualWidth) + "│"

    val result = StringBuilder()

    // Top border with optional title
    if (title != null && title.isNotEmpty()) {
        val titlePadding = (actualWidth - title.length - 2) / 2
        val leftPadding = titlePadding
        val rightPadding = actualWidth - title.length - 2 - leftPadding

        result.append(
            "${color.colorCode}${style.styleCode}┌─${title}${
                "─".repeat(rightPadding)
            }┐${CommandLineStyles.RESET_ALL.styleCode}\n"
        )
    } else {
        result.append("${color.colorCode}${style.styleCode}┌${horizontalBorder}┐${CommandLineStyles.RESET_ALL.styleCode}\n")
    }

    // Top padding
    repeat(padding) {
        result.append("${color.colorCode}${style.styleCode}${emptyLine}${CommandLineStyles.RESET_ALL.styleCode}\n")
    }

    // Content
    lines.forEach { line ->
        val paddedLine = line.padEnd(maxLineLength)
        val leftPadding = " ".repeat(padding)
        val rightPadding = " ".repeat(actualWidth - paddedLine.length - padding)

        result.append("${color.colorCode}${style.styleCode}│${leftPadding}${CommandLineStyles.RESET_ALL.styleCode}")
        result.append(paddedLine)
        result.append("${color.colorCode}${style.styleCode}${rightPadding}│${CommandLineStyles.RESET_ALL.styleCode}\n")
    }

    // Bottom padding
    repeat(padding) {
        result.append("${color.colorCode}${style.styleCode}${emptyLine}${CommandLineStyles.RESET_ALL.styleCode}\n")
    }

    // Bottom border
    result.append("${color.colorCode}${style.styleCode}└${horizontalBorder}┘${CommandLineStyles.RESET_ALL.styleCode}")

    return result.toString()
}

/**
 * Prints text inside a box.
 *
 * @param text The text to put in the box.
 * @param padding The padding around the text.
 * @param color The color of the box.
 * @param style The style of the box.
 * @param title Optional title for the box.
 */
fun printTextBox(
    text: String,
    padding: Int = 1,
    color: CommandLineColors = CommandLineColors.WHITE,
    style: CommandLineStyles = CommandLineStyles.BOLD,
    title: String? = null
) {
    print(createTextBox(text, padding, color, style, title))
}

