### BlowfishEncryption

A utility for performing encryption and decryption using the Blowfish algorithm.

> ⚠️ **Warning**: Blowfish is considered legacy and less secure than AES due to its 64-bit block size. Use it only for
> compatibility reasons. The object is annotated with `@CriticalAPI`, so callers must opt in.

#### Features

- Simple password-based encryption.
- Support for `String` and `ByteArray`.
- Optional GZIP compression.

#### Usage

```kotlin
@OptIn(CriticalAPI::class)
fun example() {
    val encrypted = BlowfishEncryption.encrypt("legacy data", "myPassword")
    val decrypted = BlowfishEncryption.decrypt(encrypted!!, "myPassword")
}
```
