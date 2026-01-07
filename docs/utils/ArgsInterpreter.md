### ArgsInterpreter

A lightweight parser for command-line arguments.

#### Features

- Parse key-value pairs (e.g., `key=value`).
- Custom splitting characters.
- Support for Boolean, Int, Double, and String values.
- Check for standalone flags (e.g., `-h`).

#### Usage

```kotlin
fun main(args: Array<String>) {
    val interpreter = ArgsInterpreter(args)
    val port = interpreter.getValueAsInt("port") ?: 8080
    val isDebug = interpreter.containsParam("debug")
}
```
