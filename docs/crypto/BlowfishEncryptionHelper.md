### BlowfishEncryptionHelper

A utility for performing encryption and decryption using the Blowfish algorithm.

> ⚠️ **Warning**: Blowfish is considered legacy and less secure than AES due to its 64-bit block size. Use it only for
> compatibility reasons.

#### Features

- Simple password-based encryption.
- Support for `String` and `ByteArray`.
- Optional GZIP compression.

#### Usage

```kotlin
@OptIn(CriticalAPI::class)
val encrypted = BlowfishEncryptionHelper.encryptWithBlowfish("legacy data", "myPassword")
val decrypted = BlowfishEncryptionHelper.decryptWithBlowfish(encrypted!!, "myPassword")
```
