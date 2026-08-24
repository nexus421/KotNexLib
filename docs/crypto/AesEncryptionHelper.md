### AES

A robust and user-friendly utility for AES encryption and decryption. It supports multiple modes (CBC, GCM, ECB) and
provides helpers for password-based encryption.

#### Features

- **CBC Mode**: Widely supported, uses Initialization Vectors (IV).
- **GCM Mode**: Authenticated encryption (confidentiality + integrity). Recommended for new projects. Marked
  `@ExperimentalKotNexLibAPI`.
- **ECB Mode**: Simple but less secure (deterministic).
- **Password-based**: PBKDF2 with HMAC SHA-256 for key derivation (600 000 iterations by default, minimum 65 536).
- **Compression**: Optional GZIP compression before encryption.

#### Usage Examples

##### GCM (Recommended)

```kotlin
// Easy encryption with password. Returns an AESData that is safe to store (e.g. AESData.toString()).
val encrypted = AES.GCM.encryptWithPassword("My Secret Data", "securePassword")
val decrypted = encrypted.decryptAsString("securePassword").getOrThrow()

// AESData can be persisted and restored later:
val restored = AES.AESData.restore(encrypted.toString()).getOrThrow()
```

##### CBC

```kotlin
val key = AES.Common.generateAESKey()
val iv = AES.Common.generateIV().getOrThrow()
val encrypted = AES.CBC.encrypt("Hello", key, iv).getOrThrow()
val decrypted = AES.CBC.decrypt(encrypted, key, iv).getOrThrow()
```

#### CBC vs GCM

| Feature         | CBC                           | GCM                                |
|-----------------|-------------------------------|------------------------------------|
| **Integrity**   | No (Needs MAC)                | Yes (Built-in)                     |
| **Performance** | Sequential                    | Parallelizable                     |
| **Security**    | Susceptible to Padding Oracle | High (Catastrophic on Nonce reuse) |

> **Note**: In our GCM implementation, nonces are automatically managed to prevent reuse in helper methods.
