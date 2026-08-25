### Terminal Output (ColoredPrinters)

Powerful tools for creating professional command-line interfaces.

#### Colors & Styles

- **Colors**: Support for standard and bright colors (Red, Green, Blue, etc.).
- **Backgrounds**: Set background colors for text.
- **Styles**: Bold, Italic, Underline, Blink, Strikethrough, and more.

#### Dynamic UI Components

- **ProgressBar**: Easily track long-running tasks.
- **Spinner**: Indeterminate loading indicators.
- **Table**: Print structured data with borders and aligned columns.
- **TextBox**: Wrap text in a stylish border.

#### Cursor Manipulation

- `up()`, `down()`, `left()`, `right()`: Move the terminal cursor.
- `clearLine()`, `clearScreen()`: Refresh the terminal view.
- `updateLine()`: Replace the current line content (great for status updates).

#### Example

```kotlin
val table = Table(headers = listOf("ID", "Status"))
table.addRow("1", "Completed")
table.print()

val spinner = createSpinner("Working...")
spinner.start()
// ... do work
spinner.stop("Done!")
```

#### `NO_COLOR` Support

`Terminal.supportsAnsiColors()` respects the [NO_COLOR standard](https://no-color.org): if the `NO_COLOR`
environment variable is set (to any value), no ANSI codes are emitted, regardless of platform. All coloring functions
and the components below check this automatically — there's nothing to opt into.

### Interactive Prompts (`kotnexlib.terminal.Prompts`)

Simple stdin prompts for CLI tools, built on top of `printColored`/`printlnColored` — they inherit `NO_COLOR`
detection automatically.

```kotlin
val name = Prompts.prompt("What's your name?", default = "Anonymous")

if (Prompts.confirm("Proceed?", default = true)) {
    // ...
}

val environment = Prompts.select("Choose an environment", listOf("dev", "staging", "prod"))
```

- `prompt()`: Reads a line of text, with an optional default for empty input.
- `confirm()`: Yes/no question; falls back to `default` for empty or unrecognized input.
- `select()`: Numbered menu; re-prompts until a valid choice is made. `options` must not be empty.
