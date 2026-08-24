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

#### FileWatcher

Reactively watches a directory for filesystem changes via Java NIO's `WatchService`, exposed as a
`kotlinx.coroutines.flow.Flow` — no manual polling required.

> [!NOTE]
> Requires `kotlinx-coroutines-core` on the consumer's classpath (like the rest of KotNexLib's optional
> integrations, it is `compileOnly` — only pulled in if you actually call `watchDirectory()`).

```kotlin
File("/path/to/watch").watchDirectory()
    .onEach { event -> println("${event.kind}: ${event.file}") }
    .launchIn(coroutineScope)
```

Each emitted `FileChangeEvent` carries the affected `file` and a `kind` (`CREATED`, `MODIFIED`, or `DELETED`).
Cancelling collection (or cancelling the enclosing coroutine scope) cleanly closes the underlying `WatchService`
and stops the background watcher thread.
