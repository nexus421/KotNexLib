### Argon2Helper

A secure utility for hashing and verifying passwords using the **Argon2id** algorithm. It follows the Argon2 version 1.3
standard and provides a self-contained hash format for easy storage and verification.

#### Features

- **Bouncy Castle Integration**: Uses the reliable Bouncy Castle library for cryptographic primitives.
- **Argon2id**: Hybrid mode providing the best protection against both GPU/ASIC attacks and side-channel attacks.
- **Customizable Cost Factors**: Adjustable memory, iterations, and parallelism.
- **Self-Contained Hashes**: Parameters and salt are encoded within the resulting string.
- **Constant-Time Verification**: Protection against timing attacks.

#### Basic Usage

```kotlin
val password = "mySecurePassword".toCharArray()

// Hash a password
val hash = Argon2Helper.hash(password)

// Verify a password
val isCorrect = Argon2Helper.verify(password, hash).getOrDefault(false)
```

#### Advanced Parameters

You can customize the security level based on your server resources:

| Parameter     | Default | Description                       |
|:--------------|:--------|:----------------------------------|
| `iterations`  | `3`     | Number of passes over the memory. |
| `memoryKb`    | `65536` | Memory usage in KiB (64 MiB).     |
| `parallelism` | `1`     | Number of threads/lanes.          |

```kotlin
// More secure (if server has enough RAM and CPU)
val strongHash = Argon2Helper.hash(
    password,
    iterations = 3,
    memoryKb = 131072, // 128 MiB
    parallelism = 4
)
```

#### Security Recommendations

- **Iterations**: At least 1 (RFC 9106) or 3 (OWASP).
- **Memory**: At least 16 MB (mobile) or 64 MB (server).
- **Parallelism**: Match the number of cores available for a single hashing operation (usually 1, 2, or 4).

> [!TIP]
> Always use `CharArray` for passwords and clear it from memory as soon as possible after hashing.
