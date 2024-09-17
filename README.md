# KotNexLib

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
- and many more...

If you have any additional useful features or wishes, tell me!

Warning: Breaking Changes with 2.0.0 and 3.0.0. Cleaned the folder structure. You may need to re-import some methods.

## Gradle Integration

To integrate KotNexLib into your project using Gradle, add the following dependency to your `build.gradle` file:

    repositories {
        maven("https://jitpack.io")
    }

    dependencies {
        implementation ("com.github.nexus421:KotNexLib:3.1.0")
    }

See releases for other versions.
