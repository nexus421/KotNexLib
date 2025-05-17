# KotNexLib

<p align="center">
  <img src="https://github.com/user-attachments/assets/dce41e84-cd28-49b8-91a7-6649e6890ad2" />
</p>

Some useful kotlin extensions and classes that I use in nearly all my projects. This can be used in all JVM-Applications
targeting Java 11 or higher.
For Android-specific extensions, take a look at my repository "Kotlin-Extensions-Android".

Examples:

- Easy hash any String with String.hash()
- Encrypt/Decrypt any String with String.decrypt() and String.encrypt()
- Encrypt/Decrypt any String with AES or Blowfish
- Conversions from and to Date/Calendar/LocalDate/LocalTime/LocalDateTime
- Compress/Decompress any String with ease
- Basic folder and file-management for Desktop/Server including a basic logging and config file handling. See
  BaseFolder, Logfile and ConfigFile
- ResultOf-Variants for better State/Error-Handling like success/error.
- Different new contains-functions for String
- Create random Strings with eas through getRandomString()
- function to measure time including a return value. See measureTime { ... }
- runsAsJar to check if you are inside an JAR or the IDE
- Extensions for ObjectBox. Dependencies are not shipped with this library.
- Enhanced terminal output with ColoredPrinters:
  - Print colored text with various foreground and background colors
  - Apply text styles (bold, italic, underline, etc.)
  - Create progress bars and spinners for long-running tasks
  - Display tables with customizable formatting
  - Update text in-place for dynamic displays
  - Cross-platform support with focus on Linux
- and many more...

If you have any additional useful features or wishes, tell me!

Warning: Breaking Changes with 2.0.0 and 3.0.0. Cleaned the folder structure. You may need to re-import some methods.

## ColoredPrinters Usage

The enhanced `ColoredPrinters` module provides powerful terminal output capabilities:

### Basic Colored Output

```kotlin
// Print colored text
CommandLineColors.RED.println("This text is red")
CommandLineColors.GREEN.println("This text is green")

// Print with background color
printlnWithBackground("This has a blue background", CommandLineBackgroundColors.BLUE)

// Print with text style
printlnWithStyle("This text is bold", CommandLineStyles.BOLD)

// Combined formatting
printlnFormatted(
  "Custom formatted text",
  CommandLineColors.YELLOW,
  CommandLineBackgroundColors.BLUE,
  CommandLineStyles.UNDERLINE
)
```

### Dynamic Displays

```kotlin
// Progress bar
val progressBar = ProgressBar(width = 40, prefix = "Loading: ")
for (i in 0..100) {
  progressBar.update(i.toDouble() / 100.0)
  // Do work...
}
progressBar.complete()

// Spinner for indeterminate progress
val spinner = createSpinner("Loading data")
spinner.start()
// Do long-running task...
spinner.stop("Task completed!")

// Update text in-place
for (i in 0..100 step 10) {
  updateLine("Processing: $i%", CommandLineColors.PURPLE)
  // Do work...
}
```

### Tables

```kotlin
val table = Table(
  headers = listOf("Name", "Age", "City"),
  columnColors = listOf(CommandLineColors.CYAN, CommandLineColors.YELLOW, CommandLineColors.GREEN)
)
table.addRow("John", "25", "New York")
table.addRow("Alice", "30", "London")
table.print()
```

See the example file at `src/main/kotlin/kotnexlib/examples/ColoredPrintersExample.kt` for a complete demonstration.

## Gradle Integration

To integrate KotNexLib into your project using Gradle, add the following dependency to your `build.gradle` file:

    repositories {
        maven("https://jitpack.io")
    }

    dependencies {
        implementation ("com.github.nexus421:KotNexLib:3.1.1")
    }

See releases for other versions.
