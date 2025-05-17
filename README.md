# KotNexLib

<p align="center">
  <img src="https://github.com/user-attachments/assets/dce41e84-cd28-49b8-91a7-6649e6890ad2" />
</p>

## Overview

KotNexLib is a comprehensive collection of Kotlin extensions and utility classes designed to enhance productivity and
simplify common programming tasks. This library can be used in all JVM applications targeting Java 11 or higher.

For Android-specific extensions, please refer to the companion repository "Kotlin-Extensions-Android".

> **Warning**: Breaking changes were introduced in versions 2.0.0 and 3.0.0. The folder structure has been reorganized,
> which may require re-importing some methods.

## Features

KotNexLib provides a wide range of utilities and extensions organized into several categories:

### String Extensions

- Hash any string with various algorithms: `String.hash()`
- Encrypt/decrypt strings: `String.encrypt()`, `String.decrypt()`
- Base64 encoding/decoding: `String.toBase64()`, `String.fromBase64()`
- Compression/decompression: `String.compress()`, `String.decompress()`
- Enhanced string comparison: `String.containsAll()`, `String.containsOneOf()`, `String.equalsOneOf()`
- String manipulation: `String.coverString()`, `String.splitEachCharBy()`

### Cryptography

- AES encryption with multiple implementation options
- Blowfish encryption support
- Secure key generation and management
- Password-based encryption

### File Management

- Basic folder and file management for desktop/server applications
- Logging system with `LogFile`
- Configuration file handling with `ConfigFile`
- Base folder structure with `BaseFolder`
- File extensions for common operations

### Collections

- Enhanced operations for Iterables, Lists, Maps, and Sets
- Permutation generation
- Specialized contains and equals methods

### Date and Time

- Conversions between Date, Calendar, LocalDate, LocalTime, and LocalDateTime
- Formatting and parsing utilities

### Terminal Output

- Enhanced terminal output with `ColoredPrinters`
- Colored text with various foreground and background colors
- Text styling (bold, italic, underline, etc.)
- Progress bars and spinners for long-running tasks
- Tables with customizable formatting
- Dynamic text updates and positioning

### Other Utilities

- `ResultOf` variants for better state/error handling
- Time measurement with return values: `measureTime { ... }`
- Runtime environment detection: `runsAsJar`
- IBAN validation and formatting
- ObjectBox extensions (dependencies not included)
- Math utilities and number extensions
- System property access helpers

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

## Installation

### Requirements

- JDK 11 or higher
- Kotlin 1.5 or higher

### Gradle

To integrate KotNexLib into your project using Gradle, add the following to your build configuration:

#### Kotlin DSL (build.gradle.kts)

```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.nexus421:KotNexLib:3.1.1")
}
```

#### Groovy DSL (build.gradle)

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.nexus421:KotNexLib:3.1.1'
}
```

Check the [releases page](https://github.com/nexus421/KotNexLib/releases) for the latest version.

## Getting Started

After adding KotNexLib to your project, you can start using its extensions and utilities immediately. Most functions are
available as extension methods, making them easy to use with your existing code.

### Basic Examples

```kotlin
// String extensions
val hashedString = "myPassword".hash()
val base64String = "Hello World".toBase64()
val originalString = base64String.fromBase64()

// Cryptography
val encrypted = "sensitive data".encryptWithAesAndPassword("mySecretPassword")
val decrypted = encrypted.decryptWithAesAndPassword("mySecretPassword")

// File management
val configFile = ConfigFile("myapp.config")
configFile.setValue("username", "user123")
val username = configFile.getValue("username")

// Time measurement
val result = measureTime {
    // Your code here
    "Operation result"
}
println("Operation took ${result.timeInMillis}ms and returned: ${result.result}")
```

