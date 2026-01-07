### AesEncryptionHelper

A robust and user-friendly utility for AES encryption and decryption. It supports multiple modes (CBC, GCM, ECB) and
provides helpers for password-based encryption.

#### Features

- **CBC Mode**: Widely supported, uses Initialization Vectors (IV).
- **GCM Mode**: Authenticated encryption (confidentiality + integrity). Recommended for new projects.
- **ECB Mode**: Simple but less secure (deterministic).
- **Password-based**: PBKDF2 with HMAC SHA-256 for key derivation.
- **Compression**: Optional GZIP compression before encryption.

#### Usage Examples

##### GCM (Recommended)

```kotlin
// Easy encryption with password
val encryption = AesEncryptionHelper.GCM.encryptWithAesAndPasswordHelper("My Secret Data", "securePassword")
val decrypted = encryption.decrypt().getOrThrow()
```

##### CBC

```kotlin
val key = AesEncryptionHelper.Common.generateAESKey()
val iv = AesEncryptionHelper.Common.getIVSecureRandom().getOrThrow()
val encrypted = AesEncryptionHelper.CBC.encryptWithAES("Hello", key, iv).getOrThrow()
```

#### CBC vs GCM

| Feature         | CBC                           | GCM                                |
|-----------------|-------------------------------|------------------------------------|
| **Integrity**   | No (Needs MAC)                | Yes (Built-in)                     |
| **Performance** | Sequential                    | Parallelizable                     |
| **Security**    | Susceptible to Padding Oracle | High (Catastrophic on Nonce reuse) |

> **Note**: In our GCM implementation, nonces are automatically managed to prevent reuse in helper methods.
