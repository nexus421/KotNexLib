### File Management

A set of classes to simplify working with files, folders, configurations, and logs in JVM applications.

#### BaseFolder

Creates and manages a root working directory.

```kotlin
val myFolder = BaseFolder(name = "MyAppData")
val subDir = myFolder.getSubfolder("cache", create = true)
```

#### ConfigFile

A simple way to manage JSON configurations. Supports loading, saving, and runtime reloads.

- **Auto-creation**: Generates a default config if the file doesn't exist.
- **Thread-safe**: Synchronized read/write operations.

#### LogFile

A lightweight logging system.

- **Timestamps**: Automatic date/time prefix for every log entry.
- **Rotation**: Automatically clears or moves logs to an "old" file when a size limit (e.g., 2MB) is reached.
- **Context**: Can log the calling class and method name automatically.

#### File Extensions

- `existsFile()`: Reliable check if a file exists.
- `writeText()` / `readText()`: Simplified file I/O.
- Additional utilities for path manipulation and file attributes.
