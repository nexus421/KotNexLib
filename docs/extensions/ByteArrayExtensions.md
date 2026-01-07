### ByteArray Extensions

Extensions for handling raw byte data.

#### Key Methods

- `toBase64()`: Encodes byte array to a Base64 string.
- `compress()`: Compresses bytes using GZIP and encodes the result as Base64.
- `decompress()`: Decodes Base64 and decompresses GZIP data back to original bytes.

#### Usage

```kotlin
val data = "Sample data".toByteArray()
val compressed = data.compress()
val decompressed = compressed?.decompress()
```
