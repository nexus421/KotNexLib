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
