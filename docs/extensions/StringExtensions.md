### String Extensions

A collection of powerful extension functions for `String`.

#### Hashing

- `hash(HashAlgorithm)`: Hash strings with MD5, SHA-1, SHA-256 (default), etc.
- `toBase64()`: Encode to Base64.
- `fromBase64()`: Decode from Base64.

#### Security & Manipulation

- `coverString(start, end, char)`: Partially mask a string (e.g., `H****o`).
- `encrypt()` / `decrypt()`: Basic encryption wrappers.
- `compress()` / `decompress()`: GZIP compression for large strings.

#### Validation

- `isDigitOnly`: Returns true if string contains only digits.
- `isAlphabeticOnly`: Returns true if string contains only letters.
- `isValidIban()`: Validates IBAN format.

#### Comparison

- `containsAll(list)`: Checks if all items in list are present.
- `containsOneOf(list)`: Checks if at least one item is present.
- `equalsOneOf(list)`: Checks if string matches any in the list.

#### Utility

- `toDate(pattern)`: Parses string to `Date`.
- `copyToClipboard()`: Copies string to system clipboard.
- `ifNullOrBlank { ... }`: Conditional execution for null/blank strings.
